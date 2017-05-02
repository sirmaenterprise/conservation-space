/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * Script that is responsible for init cmf dms system.
 *
 * @author borislav banchev
 */
public class InitCMFScript extends BaseAlfrescoScript {
	private static final String GROUP_ADMINISTRATORS = "GROUP_ALFRESCO_ADMINISTRATORS";

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String runAsUser = AuthenticationUtil.getRunAsUser().toLowerCase();
		if (AuthenticationUtil.getAdminUserName().equalsIgnoreCase(runAsUser)
				|| runAsUser.equalsIgnoreCase(getTenantAdmin())
				|| SEIPTenantIntegration.getSystemUser().equalsIgnoreCase(runAsUser)) {
			return super.executeImpl(req, status, cache);
		}
		throw new WebScriptException(401,
				"Attempted non admin access '" + runAsUser + "'! Operation could not be completed!");
	}

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		List<NodeRef> results = new ArrayList<NodeRef>(1);
		Map<String, Object> model = new HashMap<String, Object>();
		AuthorityService authorityService = serviceRegistry.getAuthorityService();
		String adminUser = null;
		try {
			adminUser = getTenantAdmin();

			// we have checked that runAs user is admin - now init in group
			Set<String> authoritiesForUser = authorityService.getAuthoritiesForUser(adminUser);
			if (!authoritiesForUser.contains(GROUP_ADMINISTRATORS)) {
				authorityService.addAuthority(GROUP_ADMINISTRATORS, adminUser);
			}
		} catch (Exception e) {
			log(Level.WARN, e, "Failed to add " + adminUser + " to administrators group!");
		}
		try {
			String content = req.getContent().getContent();
			JSONObject request = new JSONObject(content);
			// if path is provided
			if (request.has(KEY_START_PATH)) {
				NodeRef basePath = getCaseService().getNodeByPath(request.getString(KEY_START_PATH));
				populateStructureUnderNode(results, request, basePath);
			}
			// if site/s are provided
			if (request.has(KEY_SITES_IDS)) {
				String string = request.getString(KEY_SITES_IDS);
				String[] sites = string.split(",");
				for (String site : sites) {
					SiteInfo siteInfo = getOrCreateSite(site);
					populateStructureUnderNode(results, request, siteInfo.getNodeRef());
				}
			}
			model.put("results", results);
		} catch (Exception e) {
			log(Level.ERROR, e, "Error during initialization: ", e.getMessage());
		}

		return model;
	}

	private SiteInfo getOrCreateSite(String site) {
		SiteService siteService = getServiceRegistry().getSiteService();
		SiteInfo siteInfo = siteService.getSite(site);
		if (siteInfo == null) {
			getLogger().warn("Site " + site + "  is not found and initialized! Going to create it!");
			siteInfo = siteService.createSite("site-dashboard", site, "SEIP", "SEIP Initialized",
					org.alfresco.service.cmr.site.SiteVisibility.PUBLIC);
			Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
			props.put(ContentModel.PROP_NAME, "documentLibrary");
			props.put(SiteModel.PROP_COMPONENT_ID, "documentLibrary");
			props.put(ContentModel.PROP_DESCRIPTION, "Document Library");
			nodeService.createNode(siteInfo.getNodeRef(),
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "documentLibrary"),
					ContentModel.TYPE_FOLDER,
					props);
		}
		return siteInfo;
	}

	/**
	 * Populate structure under specified node. This is the actual worker for initializing.
	 *
	 * @param results
	 *            the results to update
	 * @param request
	 *            the request
	 * @param basePath
	 *            the base node the initialize.
	 * @throws JSONException
	 *             on parsing error
	 */
	private void populateStructureUnderNode(List<NodeRef> results, JSONObject request, NodeRef basePath)
			throws JSONException {
		JSONObject caseDefinition = request.getJSONObject("case");
		JSONObject instanceDefinition = request.getJSONObject("instance");
		JSONObject documentDefinition = request.getJSONObject("document");
		JSONObject templateDefinition = request.getJSONObject("template");
		JSONObject workflowDefinition = request.getJSONObject("workflow");
		JSONObject genericDefinition = request.getJSONObject("generic");
		JSONObject permissionDefinition = request.getJSONObject("permission");
		JSONObject taskDefinition = request.getJSONObject("task");
		// create cmf definition space
		Map<QName, Serializable> tempMap = null;
		// init all the needed spaces
		// case
		tempMap = toMap(caseDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFCaseSpace = getCaseService().createCMFCaseDefinitionSpace(basePath, tempMap);
		// document
		tempMap = toMap(documentDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFDocumentSpace = getCaseService().createCMFDocumentDefinitionSpace(basePath, tempMap);
		// workflow
		tempMap = toMap(workflowDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFWorkflowSpace = getCaseService().createCMFWorkflowDefinitionSpace(basePath, tempMap);
		// task
		tempMap = toMap(taskDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFTaskSpace = getCaseService().createCMFWorkflowTaskDefinitionSpace(basePath, tempMap);
		// instances
		tempMap = toMap(instanceDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFInstancesSpace = getCaseService().createCMFInstancesSpace(basePath, tempMap);
		// template
		tempMap = toMap(templateDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFTemplateSpace = getCaseService().createCMFSpace(basePath,
				CMFModel.TYPE_CMF_TEMPLATE_DEF_SPACE, tempMap, false);
		// generic
		tempMap = toMap(genericDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFGenericSpace = getCaseService().createCMFSpace(basePath, CMFModel.TYPE_CMF_GENERIC_DEF_SPACE,
				tempMap, false);
		// permissions
		tempMap = toMap(permissionDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFPermissionSpace = getCaseService().createCMFSpace(basePath,
				CMFModel.TYPE_CMF_PERMISSIONS_DEF_SPACE, tempMap, false);
		initSpecificStructures(results, request, basePath);
		results.add(createdCMFInstancesSpace);
		results.add(createdCMFWorkflowSpace);
		results.add(createdCMFTaskSpace);
		results.add(createdCMFCaseSpace);
		results.add(createdCMFDocumentSpace);
		results.add(createdCMFTemplateSpace);
		results.add(createdCMFGenericSpace);
		results.add(createdCMFPermissionSpace);
		tempMap = null;
	}

	/**
	 * Inits the specific structures for sub implementation classes
	 *
	 * @param results
	 *            the results
	 * @param request
	 *            the request
	 * @param basePath
	 *            the base path
	 * @return true, if successful
	 * @throws JSONException
	 */
	protected boolean initSpecificStructures(List<NodeRef> results, JSONObject request, NodeRef basePath)
			throws JSONException {
		return false;
	}

}
