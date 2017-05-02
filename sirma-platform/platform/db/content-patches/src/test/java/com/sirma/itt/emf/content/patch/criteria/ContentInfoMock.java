package com.sirma.itt.emf.content.patch.criteria;

import java.io.InputStream;
import java.io.Serializable;

import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.ContentMetadata;

/**
 * Package protected class for testing purposes for migrating search criteria.
 *
 * @author Mihail Radkov
 *
 */
class ContentInfoMock implements ContentInfo {

	private static final long serialVersionUID = -8759797973503182869L;

	private final String name;
	private final String instanceId;
	private final InputStream inputStream;
	private final boolean exists;
	private final String purpose;

	/**
	 * Constructs new content info for testing.
	 *
	 * @param name
	 *            - the content name
	 * @param instanceId
	 *            - the instance ID
	 * @param inputStream
	 *            - the content stream
	 * @param exists
	 *            - if the content exists or not
	 * @param purpose
	 *            - the content purpose
	 */
	public ContentInfoMock(String name, String instanceId, InputStream inputStream, boolean exists, String purpose) {
		this.name = name;
		this.instanceId = instanceId;
		this.inputStream = inputStream;
		this.exists = exists;
		this.purpose = purpose;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getContainerId() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public void close() {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContentId() {
		return null;
	}

	@Override
	public Serializable getInstanceId() {
		return instanceId;
	}

	@Override
	public String getContentPurpose() {
		return purpose;
	}

	@Override
	public boolean exists() {
		return exists;
	}

	@Override
	public String getMimeType() {
		return null;
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public boolean isView() {
		return false;
	}

	@Override
	public String getCharset() {
		return null;
	}

	@Override
	public String getRemoteId() {
		return null;
	}

	@Override
	public String getRemoteSourceName() {
		return null;
	}

	@Override
	public ContentMetadata getMetadata() {
		return null;
	}

	@Override
	public boolean isIndexable() {
		return false;
	}

	@Override
	public boolean isReuseable() {
		return false;
	}

}