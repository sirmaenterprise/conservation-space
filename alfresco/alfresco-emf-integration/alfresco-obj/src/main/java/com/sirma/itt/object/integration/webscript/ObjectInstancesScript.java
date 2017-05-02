package com.sirma.itt.object.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;
import com.sirma.itt.cmf.integration.webscript.CaseInstancesScript;
import com.sirma.itt.object.integration.service.DOMService;

/**
 * Script for woriking with projects.
 *
 * @author bbanchev
 */
public class ObjectInstancesScript extends CaseInstancesScript {

	private DOMService domService;

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
			debug("DOM request: ", serverPath, " data: ", content);
			if (serverPath.contains("/dom/objectinstance/create")) {
				requestPath = createRequest(value, requestPath, content);
				model.put("parent", requestPath);
			} else if (serverPath.contains("/dom/objectinstance/update")) {
				updateRequest(value, content);
			} else if (serverPath.contains("/dom/objectinstance/delete")) {
				deleteRequest(value, content);
			}
		} catch (Exception e) {
			throw new WebScriptException("Error during object operation: " + e.getMessage(), e);
		}
		model.put("results", value);
		debug("DOM request: ", serverPath, " response: ", model);
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
	private NodeRef createRequest(ArrayList<NodeRef> value, NodeRef requestPath, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = domService.getCMFObjectInstanceSpace(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = serviceRegistry.getSiteService().getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = domService.getCMFObjectInstanceSpace(site.getNodeRef());
			}
		} else if (request.has(KEY_DEFINITION_ID)) {
			NodeRef nodeRef = cmfService.getNodeRef(request.getString(KEY_DEFINITION_ID));
			if (nodeRef != null) {
				requestPath = domService.getCMFObjectInstanceSpace(nodeRef);
			}
		}
		if (requestPath == null) {
			throw new WebScriptException(404, "Store location for requested object not found!");
		}
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));

		// try use the provided cm:name
		if (properties.get(ContentModel.PROP_NAME) == null) {
			// do a mapping
			String projectName = properties.get(CMFModel.PROP_IDENTIFIER) != null
					? properties.get(CMFModel.PROP_IDENTIFIER).toString() : "object_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, projectName);
		}
		NodeRef createdProjectSpace = domService.createCMFObjectSpace(requestPath, properties);
		getOwnableService().setOwner(createdProjectSpace, CMFService.getSystemUser());

		cmfLockService.lockNode(createdProjectSpace);
		return createdProjectSpace;
	}

	/**
	 * Update request.
	 *
	 * @param value
	 *            the value
	 * @param content
	 *            the content
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void updateRequest(ArrayList<NodeRef> value, String content) throws JSONException {
		// updates a project
		JSONObject request = new JSONObject(content);
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		if (request.has(KEY_NODEID)) {
			NodeRef updateNode = updateNode(properties, domService.getNodeRef(request.getString(KEY_NODEID)));
			if (updateNode != null) {
				value.add(updateNode);
			}
		}
	}

	/**
	 * Delete request.
	 *
	 * @param value
	 *            the value
	 * @param content
	 *            the content
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void deleteRequest(ArrayList<NodeRef> value, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		if (request.has(KEY_NODEID)) {
			boolean force = request.has(KEY_FORCE) ? Boolean.valueOf(request.getString(KEY_FORCE)) : Boolean.FALSE;
			NodeRef deletable = domService.getNodeRef(request.getString(KEY_NODEID));
			if (deletable != null) {
				if (force) {
					// if force just delete all

					NodeRef parentRef = nodeService.getPrimaryParent(deletable).getParentRef();
					// delete
					nodeService.deleteNode(deletable);
					// on delete add the parent
					value.add(parentRef);

				} else {
					// do delete operation
					// archiveNode(value, request, deletable,
					// ArchiveOperation.DELETE);
				}
			}
		}
	}

	/**
	 * @return the domService
	 */
	public DOMService getDomService() {
		return domService;
	}

	/**
	 * @param domService
	 *            the domService to set
	 */
	public void setDomService(DOMService domService) {
		this.domService = domService;
	}

}
