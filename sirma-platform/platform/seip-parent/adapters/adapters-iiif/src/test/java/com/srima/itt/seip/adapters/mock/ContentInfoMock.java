package com.srima.itt.seip.adapters.mock;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentMetadata;

/**
 * Mock for the ContentInfo class.
 *
 * @author Nikolay Ch
 */
public class ContentInfoMock implements ContentInfo {

	private static final long serialVersionUID = 1L;
	private String contentId;
	private String name;
	private InputStream in = null;
	private Map<String, Serializable> metadata;

	/**
	 * Constructor with the only obligatory field the content id.
	 *
	 * @param contentId
	 *            the content id
	 */
	public ContentInfoMock(String contentId) {
		this.contentId = contentId;
	}

	/**
	 * Constructor with the only obligatory field the content id.
	 *
	 * @param contentId
	 *            the content id
	 * @param metadata
	 *            the metadata
	 */
	public ContentInfoMock(String contentId, Map<String, Serializable> metadata) {
		this.contentId = contentId;
		this.metadata = metadata;
	}

	public void setInputStream(InputStream in){
		this.in = in;
	}

	public void setName(String name){
		this.name = name;
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
		return in;
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
		return contentId;
	}

	@Override
	public Serializable getInstanceId() {
		return contentId;
	}

	@Override
	public String getContentPurpose() {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
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
		return contentId;
	}

	@Override
	public String getRemoteSourceName() {
		return null;
	}

	@Override
	public ContentMetadata getMetadata() {
		return ContentMetadata.from(metadata);
	}

	@Override
	public boolean isIndexable() {
		return false;
	}

	@Override
	public boolean isReuseable() {
		return false;
	}

	@Override
	public String getChecksum() {
		return null;
	}

}
