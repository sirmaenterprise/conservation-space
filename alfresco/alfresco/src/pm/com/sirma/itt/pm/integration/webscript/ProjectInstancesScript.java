package com.sirma.itt.pm.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript;
import com.sirma.itt.pm.integration.service.PMService;

/**
 * Script for woriking with projects.
 *
 * @author bbanchev
 *
 */
public class ProjectInstancesScript extends BaseAlfrescoScript {

	private PMService pmService;

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>();
		ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
		NodeRef requestPath = null;
		String serverPath = req.getServicePath();
		try {
			String content = req.getContent().getContent();
			debug("Project request: ", serverPath, " data: ", content);
			if (serverPath.contains("/pm/projectinstance/create")) {
				requestPath = createRequest(value, requestPath, content);
				model.put("parent", requestPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException(e.getMessage());
		}
		model.put("results", value);
		debug("Project request: ", serverPath, " response: ", model);
		return model;
	}

	/**
	 * Creates the request.
	 *
	 * @param value
	 *            the value
	 * @param requestPath
	 *            the request path
	 * @param content
	 *            the content
	 * @return the node ref
	 * @throws JSONException
	 *             the jSON exception
	 */
	private NodeRef createRequest(ArrayList<NodeRef> value, NodeRef requestPath, String content)
			throws JSONException {
		JSONObject request = new JSONObject(content);
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = pmService.getCMFProjectInstanceSpace(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = serviceRegistry.getSiteService()
					.getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = pmService.getCMFProjectInstanceSpace(site.getNodeRef());
			}
		} else if (request.has(KEY_DEFINITION_ID)) {
			NodeRef nodeRef = caseService.getNodeRef(request.getString(KEY_DEFINITION_ID));
			if (nodeRef != null) {
				requestPath = pmService.getCMFProjectInstanceSpace(nodeRef);
			}
		}
		if (requestPath == null) {
			throw new WebScriptException(404, "Store location for requested case not found!");
		}
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		// try use the provided cm:name
		if (properties.get(ContentModel.PROP_NAME) == null) {
			String caseName = "project_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, caseName);
		}
		NodeRef createdProjectSpace = pmService.createCMFProjectSpace(requestPath, properties);
		getOwnableService().setOwner(createdProjectSpace, AuthenticationUtil.getSystemUserName());

		cmfLockService.lockNode(createdProjectSpace);
		return createdProjectSpace;
	}

	/**
	 * @return the pmService
	 */
	public PMService getPmService() {
		return pmService;
	}

	/**
	 * @param pmService the pmService to set
	 */
	public void setPmService(PMService pmService) {
		this.pmService = pmService;
	}

}
