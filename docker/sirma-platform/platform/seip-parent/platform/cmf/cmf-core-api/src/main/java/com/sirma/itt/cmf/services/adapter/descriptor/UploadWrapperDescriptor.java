/*
 *
 */
package com.sirma.itt.cmf.services.adapter.descriptor;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.seip.io.CountingInputStream;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The UploadWrapperDescriptor is adapter to wrap the original descriptor along with needed data for upload.
 *
 * @author bbanchev
 */
public class UploadWrapperDescriptor implements FileAndPropertiesDescriptor {

	private static final long serialVersionUID = -7945895120650702107L;

	private String containerId;

	private Map<String, Serializable> properties;

	private FileDescriptor delegate;

	private UploadMode uploadMode = UploadMode.DIRECT;

	private CountingInputStream countingStream;

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
		delegate = descriptor;
		this.containerId = containerId;
		this.properties = properties;
	}

	/**
	 * Instantiates a new upload wrapper descriptor.
	 *
	 * @param descriptor
	 *            the descriptor with initialized properties and container
	 */
	public UploadWrapperDescriptor(FileAndPropertiesDescriptor descriptor) {
		delegate = descriptor;
		containerId = descriptor.getContainerId();
		properties = descriptor.getProperties();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public InputStream getInputStream() {
		countingStream = new CountingInputStream(delegate.getInputStream());
		return countingStream;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * Get the delegate descriptor.
	 *
	 * @return the original descriptor
	 */
	public FileDescriptor getDelegate() {
		return delegate;
	}

	public UploadMode getUploadMode() {
		return uploadMode;
	}

	public void setUploadMode(UploadMode uploadMode) {
		this.uploadMode = uploadMode;
	}

	/**
	 * Uploaded size if the content was read at all
	 *
	 * @return the long
	 */
	public long uploadedSize() {
		return countingStream == null ? -1L : countingStream.getCount();
	}

	@Override
	public long length() {
		return delegate.length();
	}

	@Override
	public void close() {
		if (delegate != null) {
			delegate.close();
		}
	}

}