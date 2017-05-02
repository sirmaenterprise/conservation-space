/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.LockUtils;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * The class holds some common node operations that are not for specific case.
 *
 * @author bbanchev
 */
public class NodeOperation extends BaseFormScript {

	private static final String KEY_PARENT_REF = "parentRef";
	/** The Constant MSG_AUTO_VERSION_PROPS. */
	private static final String MSG_AUTO_VERSION_PROPS = "create_version.auto_version_props";

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
		JSONObject request = null;
		try {

			String serverPath = req.getServicePath();
			request = new JSONObject(req.getContent().getContent());
			if (serverPath.contains("/cmf/node/update")) {
				if (request.has(KEY_NODEID)) {
					NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_NODEID));
					if (nodeService.hasAspect(updateable, ContentModel.ASPECT_WORKING_COPY)) {
						throw createStatus(500, "Node: " + updateable + " is working copy!");
					}
					Map<QName, Serializable> before = nodeService.getProperties(updateable);
					Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
					NodeRef updateNode = updateNode(properties, updateable);
					if (updateNode != null) {
						value.add(updateNode);
						Map<String, String> versionInfo = new HashMap<String, String>();
						Version currentVersion = getServiceRegistry().getVersionService().getCurrentVersion(updateNode);
						if (currentVersion != null) {
							Version currentVersionLocal = onUpdateProperties(updateNode, before,
									nodeService.getProperties(updateable), request);
							if (currentVersionLocal != null) {
								currentVersion = currentVersionLocal;
							}
							versionInfo.put(updateNode.toString(), currentVersion.getVersionLabel());
						} else {
							versionInfo.put(updateNode.toString(), "1.0");
						}
						model.put("versionInfo", versionInfo);
					}
				}

			} else if (serverPath.contains("/cmf/node/link")) {
				if (request.has(KEY_NODEID) && request.has(KEY_REFERENCE_ID)) {
					NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_NODEID));
					NodeRef reference = cmfService.getNodeRef(request.getString(KEY_REFERENCE_ID));
					String assocName = request.has(KEY_CHILD_ASSOC_NAME) ? request.getString(KEY_CHILD_ASSOC_NAME)
							: "child";
					ChildAssociationRef createdAssoc = nodeService.addChild(updateable, reference,
							CMFModel.ASSOC_CHILD_PARENT_REF, QName.createQName(CMFModel.CMF_MODEL_1_0_URI, assocName));
					model.put("parent", createdAssoc.getParentRef());
					value.add(createdAssoc.getChildRef());
				}

			} else if (serverPath.contains("/cmf/node/relink")) {
				if (request.has(KEY_NODEID) && request.has(KEY_REFERENCE_ID) && request.has(KEY_PARENT_REF)) {
					NodeRef updateable = cmfService.getNodeRef(request.getString(KEY_NODEID));
					NodeRef reference = cmfService.getNodeRef(request.getString(KEY_REFERENCE_ID));
					// remove the old parent-child
					NodeRef oldParent = cmfService.getNodeRef(request.getString(KEY_PARENT_REF));
					getNodeService().removeChild(oldParent, reference);

					String assocName = request.has(KEY_CHILD_ASSOC_NAME) ? request.getString(KEY_CHILD_ASSOC_NAME)
							: "child";
					ChildAssociationRef createdAssoc = nodeService.addChild(updateable, reference,
							CMFModel.ASSOC_CHILD_PARENT_REF, QName.createQName(CMFModel.CMF_MODEL_1_0_URI, assocName));
					model.put("parent", createdAssoc.getParentRef());
					value.add(createdAssoc.getChildRef());
				}

			} else if (serverPath.contains("/cmf/node/folder/create")) {
				NodeRef createRequest = createFolderRequest(request);
				value.add(createRequest);
			}
			model.put("results", value);
		} catch (Exception e) {
			if (e.getMessage() != null) {
				throw new WebScriptException(500, e.getMessage() + " :" + request, e);
			}
			throw new WebScriptException(500, "Erorr during operation :" + request, e);
		}
		return model;
	}

	/**
	 * Create folder based on the request object. Parent could be xpath
	 * expression, site id, ot node id. Custom type, properties and aspects
	 * could be added as well
	 *
	 * @param request
	 *            is the json request
	 * @return the created noderef
	 * @throws Exception
	 *             on initialization error or any other
	 */
	private NodeRef createFolderRequest(JSONObject request) throws Exception {
		NodeRef requestPath = null;
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = cmfService.getNodeByPath(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = serviceRegistry.getSiteService().getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = site.getNodeRef();
			}
		} else if (request.has(KEY_NODEID)) {
			requestPath = cmfService.getNodeRef(request.getString(KEY_NODEID));
		}
		if (requestPath == null) {
			throw new WebScriptException(404, "Store location for requested folder not found!");
		}
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		Collection<QName> aspects = null;
		if (request.has(KEY_ASPECTS)) {
			aspects = toAspectList(request.getJSONArray(KEY_ASPECTS));
		}
		QName type = null;
		if (request.has(KEY_TYPE)) {
			type = QName.resolveToQName(getNamespaceService(), request.getString(KEY_TYPE));
		}
		if (type == null) {
			getLogger().warn("Falling back to default type for folder");
			type = ContentModel.TYPE_FOLDER;
		}
		// try use the provided cm:name
		if (properties.get(ContentModel.PROP_NAME) == null) {
			// do a mapping
			String caseName = properties.get(CMFModel.PROP_IDENTIFIER) != null
					? properties.get(CMFModel.PROP_IDENTIFIER).toString() : "folder_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, caseName);
		}

		NodeRef createdFolder = cmfService.createCMFSpace(requestPath, type, properties, true);
		if (aspects != null) {
			HashMap<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(1);
			for (QName qName : aspects) {
				nodeService.addAspect(createdFolder, qName, aspectProperties);
			}
		}
		getOwnableService().setOwner(createdFolder, CMFService.getSystemUser());
		cmfLockService.lockNode(createdFolder);
		return createdFolder;
	}

	/**
	 * Converts array of strings to set of alfresco aspects if they are valid.
	 *
	 * @param jsonArray
	 *            is the input data
	 * @return the collection of aspects
	 * @throws Exception
	 *             on json error or any other
	 */
	private Collection<QName> toAspectList(JSONArray jsonArray) throws Exception {
		Collection<QName> aspects = new HashSet<QName>();
		for (int i = 0; i < jsonArray.length(); i++) {
			String nextAspect = jsonArray.getString(i);
			QName resolvedToQName = QName.resolveToQName(getNamespaceService(), nextAspect);
			if (resolvedToQName != null) {
				boolean contained = getDataDictionaryService().getAllAspects().contains(resolvedToQName);
				if (contained) {
					aspects.add(resolvedToQName);
				} else {
					warn("Not found aspect - ", resolvedToQName, ". Skipping it!");
				}
			} else {
				warn("Not a valid aspect - ", nextAspect, ". Skipping it!");
			}
		}
		return aspects;
	}

	/**
	 * On update properties policy behaviour mimic.
	 *
	 * @param nodeRef
	 *            the node to create version for
	 * @param before
	 *            the before the properties before update
	 * @param after
	 *            the after the properties after update
	 * @param request
	 *            the request is the http request
	 * @return the created new version or null on none created
	 * @throws JSONException
	 *             on parse error
	 */
	public Version onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after,
			JSONObject request) throws JSONException {
		if (
		// (this.nodeService.exists(nodeRef) == true)&&
		!LockUtils.isLockedAndReadOnly(nodeRef, serviceRegistry.getLockService())
				&& (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
				&& (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) == false)) {
			// onUpdatePropertiesBehaviour.disable();
			// try {
			// Create the auto-version
			Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(4);
			String description = I18NUtil.getMessage(MSG_AUTO_VERSION_PROPS);
			if (request.has(KEY_DESCRIPTION)) {
				description = request.getString(KEY_DESCRIPTION);
			}
			versionProperties.put(Version.PROP_DESCRIPTION, description);
			versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
			return getServiceRegistry().getVersionService().createVersion(nodeRef, versionProperties);
			// }
			// } finally {
			// onUpdatePropertiesBehaviour.enable();
			// }
		}
		return null;
	}
}
