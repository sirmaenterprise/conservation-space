package com.sirma.itt.cmf.alfresco4.descriptor;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;

/**
 * The AlfrescoFileAndPropertiesDescriptor is extension of {@link AlfrescoFileDescriptor} that provides the file
 * converted (cmf) properties .
 */
public class AlfrescoFileAndPropertiesDescriptor extends AlfrescoFileDescriptor implements FileAndPropertiesDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4209440791941278425L;

	/** The properties. */
	private Map<String, Serializable> properties;

	/**
	 * Instantiates a new alfresco file and properties descriptor impl.
	 *
	 * @param id
	 *            the nodeid
	 * @param containerId
	 *            the container id
	 * @param properties
	 *            the properties
	 * @param restClient
	 *            the rest client
	 */
	public AlfrescoFileAndPropertiesDescriptor(String id, String containerId, Map<String, Serializable> properties,
			RESTClient restClient) {
		super(id, containerId, restClient);
		this.properties = properties;
	}

	/**
	 * Instantiates a new alfresco file and properties descriptor impl.
	 *
	 * @param id
	 *            the nodeid
	 * @param properties
	 *            the properties
	 * @param restClient
	 *            the rest client
	 */
	public AlfrescoFileAndPropertiesDescriptor(String id, Map<String, Serializable> properties, RESTClient restClient) {
		super(id, null, restClient);
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

}
