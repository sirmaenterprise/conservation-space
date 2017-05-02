package com.sirma.itt.pm.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.integration.webscript.InitCMFScript;
import com.sirma.itt.pm.integration.service.PMService;

/**
 * The InitPMScript extends cmf initialization by adding specific project
 * structures.
 */
public class InitPMScript extends InitCMFScript {

	/** The pm service. */
	private PMService pmService;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.sirma.itt.cmf.integration.webscript.InitCMFScript#initSpecificStructures
	 * (ArrayList, JSONObject, NodeRef)
	 */
	@Override
	protected boolean initSpecificStructures(ArrayList<NodeRef> results, JSONObject request,
			NodeRef basePath) throws JSONException {
		JSONObject projectDefinitions = null;
		if (request.has("project")) {
			projectDefinitions = request.getJSONObject("project");
		}
		JSONObject projectInstances = null;
		if (request.has("projectinstance")) {
			projectInstances = request.getJSONObject("projectinstance");
		}
		Map<QName, Serializable> tempMap = null;
		if (projectDefinitions != null) {
			tempMap = toMap(projectDefinitions.getJSONObject(KEY_PROPERTIES));
			NodeRef createdCMFProjectSpace = getPmService().createCMFProjectDefinitionSpace(
					basePath, tempMap);
			results.add(createdCMFProjectSpace);
		}
		if (projectInstances != null) {
			tempMap = toMap(projectInstances.getJSONObject(KEY_PROPERTIES));
			NodeRef createdCMFProjectInstanceSpace = getPmService().createCMFProjectInstancesSpace(
					basePath, tempMap);
			results.add(createdCMFProjectInstanceSpace);
		}
		return true;
	}

	/**
	 * Gets the pm service.
	 *
	 * @return the pm service
	 */
	public PMService getPmService() {
		return pmService;
	}

	/**
	 * Sets the pm service.
	 *
	 * @param pmService
	 *            the new pm service
	 */
	public void setPmService(PMService pmService) {
		this.pmService = pmService;
	}
}
