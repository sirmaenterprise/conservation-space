/**
 *
 */
package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * The SecuirtyOperationScript is responsible to update case istances documents
 * with provided data.
 *
 * @author bbanchev
 */
public class SecuirtyOperationScript extends BaseAlfrescoScript {

	private static final String KEY_COPYABLE_PROPS = "copyable";

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
		try {
			String serverPath = req.getServicePath();
			JSONObject request = new JSONObject(req.getContent().getContent());
			if (serverPath.contains("/case/security/update/documents")) {
				String[] propertiesToCopy = null;
				if (request.has(KEY_NODEID)) {
					if (!request.has(KEY_COPYABLE_PROPS)) {
						throw new WebScriptException(404, "Missing '" + KEY_COPYABLE_PROPS
								+ "' property in model");
					}
					String copyable = request.getString(KEY_COPYABLE_PROPS);
					propertiesToCopy = (copyable == null || copyable.isEmpty() || "null"
							.equals(copyable)) ? new String[0] : copyable.split(",");

					NodeRef updateable = getCaseService().getNodeRef(request.getString(KEY_NODEID));
					if (updateable == null) {
						throw new WebScriptException(404, "Case: " + request.getString(KEY_NODEID)
								+ " not found!");
					}
					Map<QName, Serializable> updateMap = new HashMap<QName, Serializable>(
							propertiesToCopy.length);
					// retrieve info
					for (String key : propertiesToCopy) {
						QName property = QName.resolveToQName(getNamespaceService(), key);
						if (property != null) {
							updateMap.put(property,
									getNodeService().getProperty(updateable, property));
						} else {
							debug("Property:", key, " is not resolved");
						}
					}

					if (request.has(KEY_PROPERTIES)) {
						Map<QName, Serializable> map = toMap(request.getJSONObject(KEY_PROPERTIES));
						// add data to case
						nodeService.addProperties(updateable, map);
						updateMap.putAll(map);
					}
					// update each doc
					List<NodeRef> caseDocuments = caseService.getCaseDocumentsFromDB(updateable);
					for (NodeRef nodeRef : caseDocuments) {
						getNodeService().addProperties(nodeRef, updateMap);
					}
					value.add(updateable);
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

}
