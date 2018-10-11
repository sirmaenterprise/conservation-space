package com.sirma.sep.content.preview.model;

import java.io.File;

/**
 * DTO representing a request for preview & thumbnail generation of given content and mimetype.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewRequest {

	private final File content;
	private final String mimetype;
	private final String instanceId;

	private int timeoutMultiplier = 1;

	/**
	 * Instantiates a new request with the provided document content and mimetype.
	 *
	 * @param content
	 * 		- the document content for generation
	 * @param mimetype
	 * 		- the content's mimetype
	 * @param instanceId
	 * 		- id of the instance to which the content is assigned
	 */
	public ContentPreviewRequest(File content, String mimetype, String instanceId) {
		this.content = content;
		this.mimetype = mimetype;
		this.instanceId = instanceId;
	}

	public File getContent() {
		return content;
	}

	public String getMimetype() {
		return mimetype;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public int getTimeoutMultiplier() {
		return timeoutMultiplier;
	}

	public ContentPreviewRequest setTimeoutMultiplier(int timeoutMultiplier) {
		this.timeoutMultiplier = timeoutMultiplier;
		return this;
	}
}
