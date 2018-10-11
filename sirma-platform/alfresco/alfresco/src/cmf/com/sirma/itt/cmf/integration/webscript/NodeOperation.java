/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.LockUtils;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * The Class NodeOperation.
 *
 * @author bbanchev
 */
public class NodeOperation extends BaseFormScript {

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
	protected  Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
		try {

			String serverPath = req.getServicePath();
			JSONObject request = new JSONObject(req.getContent().getContent());
			if (serverPath.contains("/cmf/node/update")) {
				if (request.has(KEY_NODEID)) {
					NodeRef updateable = caseService.getNodeRef(request.getString(KEY_NODEID));
					if (nodeService.hasAspect(updateable, ContentModel.ASPECT_WORKING_COPY)) {
						throw createStatus(500, "Node: " + updateable + " is working copy!");
					}
					Map<QName, Serializable> before = nodeService.getProperties(updateable);
					Map<QName, Serializable> properties = toMap(request
							.getJSONObject(KEY_PROPERTIES));
					NodeRef updateNode = updateNode(properties, updateable);
					if (updateNode != null) {
						value.add(updateNode);
						Map<String, String> versionInfo = new HashMap<String, String>();
						Version currentVersion = getServiceRegistry().getVersionService()
								.getCurrentVersion(updateNode);
						if (currentVersion != null) {
							Version currentVersionLocal = onUpdateProperties(updateNode, before,
									nodeService.getProperties(updateable), request);
							if (currentVersionLocal != null) {
								currentVersion = currentVersionLocal;
							}
						}
						versionInfo.put(updateNode.toString(), currentVersion.getVersionLabel());
						model.put("versionInfo", versionInfo);
					}
				}

			}
			model.put("results", value);
		} catch (Exception e) {
			if (e.getMessage() != null) {
				throw new WebScriptException(500, e.getMessage());
			}
			throw new WebScriptException(500, "Erorr during update", e);
		}
		return model;
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
	public Version onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after, JSONObject request) throws JSONException {
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
			return getServiceRegistry().getVersionService().createVersion(nodeRef,
					versionProperties);
			// }
			// } finally {
			// onUpdatePropertiesBehaviour.enable();
			// }
		}
		return null;
	}
}
