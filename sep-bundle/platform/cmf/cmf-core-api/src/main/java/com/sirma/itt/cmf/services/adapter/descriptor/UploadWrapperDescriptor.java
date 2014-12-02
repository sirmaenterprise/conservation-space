/*
 *
 */
package com.sirma.itt.cmf.services.adapter.descriptor;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;

/**
 * The UploadWrapperDescriptor is adapter to wrap the original descriptor along with needed data for
 * upload
 *
 * @author bbanchev
 */
public class UploadWrapperDescriptor implements FileAndPropertiesDescriptor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7945895120650702107L;
	/** The container id. */
	private String containerId;

	/** The properties. */
	private Map<String, Serializable> properties;

	private FileDescriptor delegate;

	/**
	 * Instantiates a new upload descriptor wrapper.
	 *
	 * @param descriptor
	 *            the original descriptor
	 * @param containerId
	 *            the container id
	 * @param properties
	 *            the properties
	 */
	public UploadWrapperDescriptor(FileDescriptor descriptor, String containerId,
			Map<String, Serializable> properties) {
		this.delegate = descriptor;
		this.containerId = containerId;
		this.properties = properties;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return delegate.getId();
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
	public InputStream getInputStream() {
		return delegate.getInputStream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * Get the delegate descriptor
	 *
	 * @return the original descriptor
	 */
	public FileDescriptor getDelegate() {
		return delegate;
	}

}