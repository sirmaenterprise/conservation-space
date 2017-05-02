package com.sirma.itt.object.integration.webscript;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.integration.webscript.InitCMFScript;
import com.sirma.itt.object.integration.service.DOMService;

/**
 * The InitDOMScript extends cmf initialization by adding specific DOM structures.
 */
public class InitDOMScript extends InitCMFScript {

	/** The pm service. */
	private DOMService domService;

	@Override
	protected boolean initSpecificStructures(List<NodeRef> results, JSONObject request, NodeRef basePath)
			throws JSONException {
		JSONObject objectDefinitions = null;
		// definition space
		if (request.has("objects")) {
			objectDefinitions = request.getJSONObject("objects");
		}
		// instances space
		JSONObject objectInstances = null;
		if (request.has("objectinstance")) {
			objectInstances = request.getJSONObject("objectinstance");
		}
		Map<QName, Serializable> tempMap = null;
		if (objectDefinitions != null) {
			tempMap = toMap(objectDefinitions.getJSONObject(KEY_PROPERTIES));
			NodeRef createdCMFObjectSpace = getDomService().createCMFObjectDefinitionSpace(basePath, tempMap);
			results.add(createdCMFObjectSpace);
		}
		if (objectInstances != null) {
			tempMap = toMap(objectInstances.getJSONObject(KEY_PROPERTIES));
			NodeRef createdCMFObjectInstanceSpace = getDomService().createCMFObjectInstancesSpace(basePath, tempMap);
			results.add(createdCMFObjectInstanceSpace);
		}
		return true;
	}

	/**
	 * Gets the dom service.
	 *
	 * @return the domService
	 */
	public DOMService getDomService() {
		return domService;
	}

	/**
	 * Sets the dom service.
	 *
	 * @param domService
	 *            the domService to set
	 */
	public void setDomService(DOMService domService) {
		this.domService = domService;
	}
}
