package com.sirma.itt.pm.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.sirma.itt.cmf.integration.webscript.CaseInstancesScript;
import com.sirma.itt.pm.integration.service.PMService;

/**
 * Script for woriking with projects.
 * 
 * @author bbanchev
 */
public class ProjectInstancesScript extends CaseInstancesScript {

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
		try {
			final String serverPath = req.getServicePath();
			final String content = req.getContent().getContent();
			debug("Project request: ", serverPath, " data: ", content);

			Map<String, Object> model = new HashMap<String, Object>();
			List<NodeRef> value = new ArrayList<NodeRef>(1);
			if (serverPath.contains("/pm/projectinstance/create")) {
				model.put("parent", createRequest(value, null, content));
			} else if (serverPath.contains("/pm/projectinstance/update")) {
				updateRequest(value, content);
			} else if (serverPath.contains("/pm/projectinstance/delete")) {
				deleteRequest(value, content);
			}
			model.put("results", value);
			debug("Project request: ", serverPath, " response: ", model);
			return model;

		} catch (Exception e) {
			throw new WebScriptException("Error during project operation: " + e.getMessage(), e);
		}
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
	private NodeRef createRequest(List<NodeRef> value, NodeRef requestPath, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = pmService.getCMFProjectInstanceSpace(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = getServiceRegistry().getSiteService().getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = pmService.getCMFProjectInstanceSpace(site.getNodeRef());
			}
		} else if (request.has(KEY_DEFINITION_ID)) {
			NodeRef nodeRef = cmfService.getNodeRef(request.getString(KEY_DEFINITION_ID));
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
			// do a mapping
			String projectName = properties.get(CMFModel.PROP_IDENTIFIER) != null
					? properties.get(CMFModel.PROP_IDENTIFIER).toString() : "project_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, projectName);
		}
		NodeRef createdProjectSpace = pmService.createCMFProjectSpace(requestPath, properties);
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
	private void updateRequest(List<NodeRef> value, String content) throws JSONException {
		// updates a project
		JSONObject request = new JSONObject(content);
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		if (request.has(KEY_NODEID)) {
			NodeRef updateNode = updateNode(properties, pmService.getNodeRef(request.getString(KEY_NODEID)));
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
	private void deleteRequest(List<NodeRef> value, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		if (request.has(KEY_NODEID)) {
			boolean force = request.has(KEY_FORCE) ? Boolean.valueOf(request.getString(KEY_FORCE)) : Boolean.FALSE;
			NodeRef deletable = pmService.getNodeRef(request.getString(KEY_NODEID));
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

	// /**
	// * Archive node.
	// *
	// * @param value
	// * the value
	// * @param request
	// * the request
	// * @param updetable
	// * the updetable node
	// * @param type
	// * the type
	// * @throws JSONException
	// * the jSON exception
	// */
	// private void archiveNode(ArrayList<NodeRef> value, JSONObject request,
	// NodeRef updetable,
	// ArchiveOperation type) throws JSONException {
	// try {
	// AuthenticationUtil.pushAuthentication();
	// AuthenticationUtil.setRunAsUserSystem();
	// Map<QName, Serializable> properties =
	// toMap(request.getJSONObject(KEY_PROPERTIES));
	// List<NodeRef> caseDocuments = prepareCaseAndWorkflows(updetable,
	// properties, type,
	// request);
	// debug(caseDocuments, " with size:", caseDocuments.size(),
	// " will be closed!");
	// for (NodeRef nodeRef : caseDocuments) {
	// if (!nodeService.exists(nodeRef)) {
	// continue;
	// }
	// if (type == ArchiveOperation.DELETE) {
	// cmfLockService.getLockService().unlock(nodeRef);
	// nodeService.deleteNode(nodeRef);
	// } else if (type == ArchiveOperation.CLOSE) {
	// cmfLockService.lockNode(nodeRef);
	// }
	// }
	// value.add(updetable);
	// } finally {
	// try {
	// if (updetable != null) {
	// String lockedOwner = cmfLockService.getLockedOwner(updetable);
	// if (!AuthenticationUtil.getSystemUserName().equals(lockedOwner)) {
	// cmfLockService.getLockService().unlock(updetable);
	// cmfLockService.lockNode(updetable);
	// }
	// }
	// } catch (Exception e) {
	// throw new WebScriptException(500, "Error during lock!", e);
	// } finally {
	// AuthenticationUtil.popAuthentication();
	// }
	// }
	// }
	/**
	 * @return the pmService
	 */
	public PMService getPmService() {
		return pmService;
	}

	/**
	 * @param pmService
	 *            the pmService to set
	 */
	public void setPmService(PMService pmService) {
		this.pmService = pmService;
	}

}
