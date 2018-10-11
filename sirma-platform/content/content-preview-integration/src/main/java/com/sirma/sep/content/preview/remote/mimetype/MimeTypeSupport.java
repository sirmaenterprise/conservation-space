package com.sirma.sep.content.preview.remote.mimetype;

/**
 * Describes a MIME type support for preview and/or thumbnail generation by the remove service.
 *
 * @author Mihail Radkov
 */
public class MimeTypeSupport {

	private boolean supportsPreview = false;
	private boolean isSelfPreview = false;
	private boolean supportsThumbnail = false;

	public boolean supportsPreview() {
		return supportsPreview;
	}

	public void setSupportsPreview(boolean supportsPreview) {
		this.supportsPreview = supportsPreview;
	}

	public boolean isSelfPreview() {
		return isSelfPreview;
	}

	public void setSelfPreview(boolean selfPreview) {
		isSelfPreview = selfPreview;
	}

	public boolean supportsThumbnail() {
		return supportsThumbnail;
	}

	public void setSupportsThumbnail(boolean supportsThumbnail) {
		this.supportsThumbnail = supportsThumbnail;
	}

}
