package com.sirma.itt.pm.integration.service;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;

import com.sirma.itt.cmf.integration.service.CMFService;
import com.sirma.itt.pm.integration.model.PMModel;

/**
 * Service to handle basic backend functionality for PM tool.
 *
 * @author bbanchev
 */
public class PMService extends CMFService {

	/**
	 * Creates the cmf project definition space.
	 *
	 * @param basePath the base path
	 * @param properties the properties
	 * @return the node ref
	 * @throws JSONException the jSON exception
	 */
	public NodeRef createCMFProjectDefinitionSpace(NodeRef basePath,
			Map<QName, Serializable> properties) throws JSONException {
		return createCMFSpace(basePath, PMModel.TYPE_PM_PROJECT_DEF_SPACE, properties, false);
	}

	/**
	 * Creates the cmf project instances space.
	 *
	 * @param basePath the base path
	 * @param properties the properties
	 * @return the node ref
	 * @throws JSONException the jSON exception
	 */
	public NodeRef createCMFProjectInstancesSpace(NodeRef basePath,
			Map<QName, Serializable> properties) throws JSONException {
		return createCMFSpace(basePath, PMModel.TYPE_PM_PROJECT_INSTANCES_SPACE, properties, false);
	}

	/**
	 * Gets the cMF project instance space.
	 *
	 * @param basePathNode the base path node
	 * @return the cMF project instance space
	 */
	public NodeRef getCMFProjectInstanceSpace(NodeRef basePathNode) {
		return getCMFSpace(PMModel.TYPE_PM_PROJECT_INSTANCES_SPACE, basePathNode);
	}

	/**
	 * Gets the cMF project instance space.
	 *
	 * @param basePath the base path
	 * @return the cMF project instance space
	 */
	public NodeRef getCMFProjectInstanceSpace(String basePath) {
		return getCMFSpace(PMModel.TYPE_PM_PROJECT_INSTANCES_SPACE, basePath);
	}

	/**
	 * Creates the cmf project space.
	 *
	 * @param basePath the base path
	 * @param properties the properties
	 * @return the node ref
	 */
	public NodeRef createCMFProjectSpace(NodeRef basePath, Map<QName, Serializable> properties) {
		NodeRef basePathNew = getProjectWorkingDir(basePath);
		return createCMFSpace(basePathNew, PMModel.TYPE_PM_INSTANCE_SPACE, properties, true);
	}

	/**
	 * Gets the project working dir.
	 *
	 * @param basePath the base path
	 * @return the project working dir
	 */
	private NodeRef getProjectWorkingDir(NodeRef basePath) {
		return getWorkingDir(basePath);

	}

}
