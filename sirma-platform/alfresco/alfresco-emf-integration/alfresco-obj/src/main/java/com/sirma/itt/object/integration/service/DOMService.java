package com.sirma.itt.object.integration.service;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;

import com.sirma.itt.cmf.integration.service.CMFService;
import com.sirma.itt.object.integration.model.DOMModel;

/**
 * Service to handle basic backend functionality for DOM tool.
 *
 * @author bbanchev
 */
public class DOMService extends CMFService {
	/**
	 * Creates the cmf object instances space.
	 *
	 * @param basePath
	 *            the base path
	 * @param properties
	 *            the properties
	 * @return the node ref
	 * @throws JSONException
	 *             the jSON exception
	 */
	public NodeRef createCMFObjectInstancesSpace(NodeRef basePath,
			Map<QName, Serializable> properties) throws JSONException {
		return createCMFSpace(basePath, DOMModel.TYPE_DOM_OBJECT_INSTANCES_SPACE, properties, false);
	}

	/**
	 * Creates the cmf object definition space.
	 *
	 * @param basePath
	 *            the base path
	 * @param properties
	 *            the properties
	 * @return the node ref
	 * @throws JSONException
	 *             the jSON exception
	 */
	public NodeRef createCMFObjectDefinitionSpace(NodeRef basePath,
			Map<QName, Serializable> properties) throws JSONException {
		return createCMFSpace(basePath, DOMModel.TYPE_DOM_OBJECT_DEF_SPACE, properties, false);
	}

	/**
	 * Gets the object instance space where all instances are stored. based on node is that site
	 *
	 * @param basePathNode
	 *            the base path node
	 * @return the cMF object instance space
	 */
	public NodeRef getCMFObjectInstanceSpace(NodeRef basePathNode) {
		return getCMFSpace(DOMModel.TYPE_DOM_OBJECT_INSTANCES_SPACE, basePathNode);
	}

	/**
	 * Gets the object instance space where all instances are stored
	 *
	 * @param basePath
	 *            the base path
	 * @return the cMF object instance space
	 */
	public NodeRef getCMFObjectInstanceSpace(String basePath) {

		return getCMFSpace(DOMModel.TYPE_DOM_OBJECT_INSTANCES_SPACE, basePath);
	}

	/**
	 * Creates the cmf object space. This is the actual instance folder
	 *
	 * @param basePath
	 *            the base path
	 * @param properties
	 *            the properties
	 * @return the node ref
	 */
	public NodeRef createCMFObjectSpace(NodeRef basePath, Map<QName, Serializable> properties)
			throws JSONException {
		NodeRef basePathNew = getObjectWorkingDir(basePath);
		return createCMFSpace(basePathNew, DOMModel.TYPE_DOM_OBJECT_SPACE, properties, true);
	}

	/**
	 * Gets the object working dir.
	 *
	 * @param basePath
	 *            the base path
	 * @return the object working dir
	 */
	private NodeRef getObjectWorkingDir(NodeRef basePath) {
		return getWorkingDir(basePath);

	}

}
