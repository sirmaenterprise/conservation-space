/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

// TODO: Auto-generated Javadoc
/**
 * Script that is responsible for init cmf dms system.
 *
 * @author borislav banchev
 */
public class InitCMFScript extends BaseAlfrescoScript {

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
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setRunAsUserSystem();
			return executeInternal(req);
		} finally {
			AuthenticationUtil.popAuthentication();
		}
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
		ArrayList<NodeRef> results = new ArrayList<NodeRef>(1);
		Map<String, Object> model = new HashMap<String, Object>();

		try {
			String content = req.getContent().getContent();
			JSONObject request = new JSONObject(content);
			// if path is provided
			if (request.has(KEY_START_PATH)) {
				NodeRef basePath = getCaseService()
						.getNodeByPath(request.getString(KEY_START_PATH));
				populateStructureUnderNode(results, request, basePath);
			}
			// if site/s are provided
			if (request.has(KEY_SITES_IDS)) {
				String string = request.getString(KEY_SITES_IDS);
				String[] sites = string.split(",");
				for (String site : sites) {
					SiteInfo siteInfo = getServiceRegistry().getSiteService().getSite(site);
					if (siteInfo != null) {
						populateStructureUnderNode(results, request, siteInfo.getNodeRef());
					} else {
						LOGGER.warn("Site " + site + "  is not found and initialized!");
					}
				}
			}
			model.put("results", results);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return model;
	}

	/**
	 * Populate structure under specified node. This is the actual worker for
	 * initializing.
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
	private void populateStructureUnderNode(ArrayList<NodeRef> results, JSONObject request,
			NodeRef basePath) throws JSONException {
		JSONObject caseDefinition = request.getJSONObject("case");
		JSONObject instanceDefinition = request.getJSONObject("instance");
		JSONObject documentDefinition = request.getJSONObject("document");
		JSONObject workflowDefinition = request.getJSONObject("workflow");

		JSONObject taskDefinition = request.getJSONObject("task");
		// create cmf definition space
		Map<QName, Serializable> tempMap = null;
		// init all the needed spaces
		// case
		tempMap = toMap(caseDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFCaseSpace = getCaseService().createCMFCaseDefinitionSpace(basePath,
				tempMap);
		// document
		tempMap = toMap(documentDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFDocumentSpace = getCaseService().createCMFDocumentDefinitionSpace(
				basePath, tempMap);
		// workflow
		tempMap = toMap(workflowDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFWorkflowSpace = getCaseService().createCMFWorkflowDefinitionSpace(
				basePath, tempMap);
		// task
		tempMap = toMap(taskDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFTaskSpace = getCaseService().createCMFWorkflowTaskDefinitionSpace(
				basePath, tempMap);
		// instances
		tempMap = toMap(instanceDefinition.getJSONObject(KEY_PROPERTIES));
		NodeRef createdCMFInstancesSpace = getCaseService().createCMFInstancesSpace(basePath,
				tempMap);

		initSpecificStructures(results, request, basePath);
		results.add(createdCMFInstancesSpace);
		results.add(createdCMFWorkflowSpace);
		results.add(createdCMFTaskSpace);
		results.add(createdCMFCaseSpace);
		results.add(createdCMFDocumentSpace);
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
	protected boolean initSpecificStructures(ArrayList<NodeRef> results, JSONObject request,
			NodeRef basePath) throws JSONException {
		return false;
	}

}
