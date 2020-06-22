package com.sirma.sep.content.descriptor;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;

/**
 * The LocalFileAndPropertiesDescriptor provides access to a file binded with some properties as well to be used during
 * upload
 */
public class LocalFileAndPropertiesDescriptor extends LocalFileDescriptor implements FileAndPropertiesDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1354418762845814136L;

	/** The container id. */
	private String containerId;

	/** The properties. */
	private Map<String, Serializable> properties;

	/**
	 * Instantiates a new local file and properties descriptor.
	 *
	 * @param file
	 *            the file
	 * @param containerId
	 *            the container id
	 * @param properties
	 *            the properties
	 */
	public LocalFileAndPropertiesDescriptor(File file, String containerId, Map<String, Serializable> properties) {
		super(file);
		this.containerId = containerId;
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainerId() {
		return containerId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (containerId == null ? 0 : containerId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof LocalFileAndPropertiesDescriptor)) {
			return false;
		}
		LocalFileAndPropertiesDescriptor other = (LocalFileAndPropertiesDescriptor) obj;
		return nullSafeEquals(containerId, other.getContainerId());
	}

}
