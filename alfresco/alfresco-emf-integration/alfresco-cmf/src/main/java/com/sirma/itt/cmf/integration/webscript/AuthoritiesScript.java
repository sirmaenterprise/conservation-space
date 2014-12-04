/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authority.script.ScriptGroup;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.ReflectionUtils;

/**
 * @author bbanchev
 */
public class AuthoritiesScript extends BaseFormScript {
	/**
	 * The proxy of
	 * {@link org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizer}.
	 */
	private Object chainingUserRegistrySynchronizer;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl
	 * (org.springframework.extensions.webscripts.WebScriptRequest,
	 * org.springframework.extensions.webscripts.Status,
	 * org.springframework.extensions.webscripts.Cache)
	 */

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		return executeInternal(req);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript#executeInternal
	 * (org.springframework.extensions.webscripts.WebScriptRequest)
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>();
		String request = req.getServiceMatch().toString();
		if (request.endsWith("/cmf/authority/{userName}/groups")) {
			model.put("mode", "findcontained");
			String user = req.getServiceMatch().getTemplateVars().get("userName");
			// get all levels groups for user
			Set<String> containedAuthorities = getAuthorityService().getContainingAuthorities(
					AuthorityType.GROUP, user, false);
			Set<ScriptGroup> authorities = new HashSet<ScriptGroup>(containedAuthorities.size());
			for (String string : containedAuthorities) {
				authorities.add(new ScriptGroup(string, serviceRegistry, null));
			}
			model.put("groups", authorities);
		} else if (request.endsWith("/cmf/authority/{userName}/find")) {
			model.put("mode", "finduser");
			String user = req.getServiceMatch().getTemplateVars().get("userName");
			// get all levels groups for user
			ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
			try {
				NodeRef personNode = getPersonService().getPerson(user, true);
				value.add(personNode);
			} catch (Exception e) {
				// if not exist fail silently and try again with forced sync
				try {
					// try to force the user synchronization
					Method findMethod = ReflectionUtils.findMethod(
							chainingUserRegistrySynchronizer.getClass(), "synchronize",
							boolean.class, boolean.class, boolean.class);
					ReflectionUtils.invokeMethod(findMethod, chainingUserRegistrySynchronizer,
							false, false, true);
					NodeRef personNode = getPersonService().getPerson(user, true);
					value.add(personNode);
				} catch (Exception e1) {
					LOGGER.error("Error during forced synchronization of users: " + e.getMessage(),
							e);
				}
			}
			model.put("peoplelist", value);
		}
		return model;
	}

	/**
	 * Getter method for chainingUserRegistrySynchronizer.
	 *
	 * @return the chainingUserRegistrySynchronizer
	 */
	public Object getChainingUserRegistrySynchronizer() {
		return chainingUserRegistrySynchronizer;
	}

	/**
	 * Setter method for chainingUserRegistrySynchronizer.
	 *
	 * @param chainingUserRegistrySynchronizer
	 *            the chainingUserRegistrySynchronizer to set
	 */
	public void setChainingUserRegistrySynchronizer(Object chainingUserRegistrySynchronizer) {
		this.chainingUserRegistrySynchronizer = chainingUserRegistrySynchronizer;
	}

}
