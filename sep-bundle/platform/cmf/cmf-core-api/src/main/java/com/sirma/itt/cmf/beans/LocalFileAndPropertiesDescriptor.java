package com.sirma.itt.cmf.beans;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;

/**
 * The LocalFileAndPropertiesDescriptor provides access to a file binded with some properties as
 * well to be used during upload
 */
public class LocalFileAndPropertiesDescriptor extends LocalFileDescriptor implements
		FileAndPropertiesDescriptor {

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
	public LocalFileAndPropertiesDescriptor(File file, String containerId,
			Map<String, Serializable> properties) {
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
}
