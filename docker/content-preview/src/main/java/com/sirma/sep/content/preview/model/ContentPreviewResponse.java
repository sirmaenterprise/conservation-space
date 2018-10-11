package com.sirma.sep.content.preview.model;

import java.io.File;

/**
 * DTO containing the generated preview and/or thumbnail of a content.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewResponse {

	private File preview;
	private String thumbnail;

	public ContentPreviewResponse setPreview(File preview) {
		this.preview = preview;
		return this;
	}

	public ContentPreviewResponse setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}

	public File getPreview() {
		return preview;
	}

	public String getThumbnail() {
		return thumbnail;
	}

}
