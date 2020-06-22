/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Authority related operations
 * 
 * @author bbanchev
 */
public class AuthoritiesScript extends BaseFormScript {
	/**
	 * {@inheritDoc} Execute as runAs user directly and change later the runAs
	 * auth to specific tenant
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		// override and based on request switch to specific tenant
		return executeInternal(req);
	}

	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>();
		String request = req.getServiceMatch().toString();
		boolean authChanged = false;
		try {
			if (request.endsWith("/cmf/authority/{userName}/groups")) {
				model.put(KEY_WORKING_MODE, "findcontained");
				String user = req.getServiceMatch().getTemplateVars().get("userName");
				String systemUser = getRunAsUser(user);
				AuthenticationUtil.pushAuthentication();
				authChanged = true;
				AuthenticationUtil.setRunAsUser(systemUser);
				// get all levels groups for user
				Set<String> containedAuthorities = getAuthorityService()
						.getContainingAuthoritiesInZone(AuthorityType.GROUP, user, null, null, -1);
				Set<ScriptGroup> authorities = new HashSet<ScriptGroup>(containedAuthorities.size());
				for (String string : containedAuthorities) {
					authorities.add(new ScriptGroup(string, serviceRegistry, null));
				}
				model.put("groups", authorities);
			} else if (request.endsWith("/cmf/authority/{userName}/find")) {
				model.put(KEY_WORKING_MODE, "finduser");
				final String user = req.getServiceMatch().getTemplateVars().get("userName");
				String systemUser = getRunAsUser(user);
				AuthenticationUtil.pushAuthentication();
				authChanged = true;
				AuthenticationUtil.setRunAsUser(systemUser);
				final String tenantId = SEIPTenantIntegration.getTenantId(systemUser);
				// get all levels groups for user
				List<NodeRef> value = new ArrayList<NodeRef>(1);
				try {
					NodeRef personNode = getPersonService().getPerson(user, true);
					value.add(personNode);
				} catch (Exception e) {
					// if not exist fail silently and try again with forced sync
					try {
						cmfService.reloadLDAP(tenantId);
						value.add(getPersonService().getPerson(user, true));
					} catch (Exception e1) {
						getLogger().error("Error during forced synchronization of users: " + e1.getMessage(), e1);
					}
				}
				model.put("peoplelist", value);
			}
		} finally {
			if (authChanged) {
				AuthenticationUtil.popAuthentication();
			}
		}
		return model;
	}

	private String getRunAsUser(String user) {
		String tenantId = CMFService.getTenantId(user);
		if (tenantId == null || tenantId.isEmpty()) {
			tenantId = CMFService.getTenantId(AuthenticationUtil.getRunAsUser());
		}
		String systemUser = SEIPTenantIntegration.getSystemUserByTenantId(tenantId);
		return systemUser;
	}

}
