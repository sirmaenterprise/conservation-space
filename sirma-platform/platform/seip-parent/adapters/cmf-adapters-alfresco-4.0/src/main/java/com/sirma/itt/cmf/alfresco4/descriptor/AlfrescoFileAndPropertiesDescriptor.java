package com.sirma.itt.cmf.alfresco4.descriptor;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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
	public AlfrescoFileAndPropertiesDescriptor(String id, String containerId, String fileName, Map<String, Serializable> properties,
			RESTClient restClient) {
		super(id, containerId, fileName, restClient);
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
	public AlfrescoFileAndPropertiesDescriptor(String id, String fileName, Map<String, Serializable> properties, RESTClient restClient) {
		super(id, null, fileName, restClient);
		this.properties = properties;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!super.equals(o)) {
			return false;
		}
		if (!(o instanceof AlfrescoFileAndPropertiesDescriptor)) {
			return false;
		}
		AlfrescoFileAndPropertiesDescriptor that = (AlfrescoFileAndPropertiesDescriptor) o;
		return Objects.equals(getProperties(), that.getProperties());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getProperties());
	}
}
