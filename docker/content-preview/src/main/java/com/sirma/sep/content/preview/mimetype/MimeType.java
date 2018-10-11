package com.sirma.sep.content.preview.mimetype;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Mapping object for specific mime type and its preview & thumbnail generation support.
 *
 * @author Mihail Radkov
 */
public class MimeType {

	private String name;

	@JsonIgnore
	private MimeTypeSupport preview;

	@JsonIgnore
	private MimeTypeSupport thumbnail;

	/**
	 * @return true if the mime type is supported for a preview generation
	 */
	@JsonGetter
	public boolean supportsPreview() {
		return MimeTypeSupport.YES.equals(preview);
	}

	/**
	 * @return true if the mime type itself is a preview
	 */
	public boolean isSelfPreview() {
		return MimeTypeSupport.SELF.equals(preview);
	}

	/**
	 * @return true if the mimetype is supported for a thumbnail generation
	 */
	@JsonGetter
	public boolean supportsThumbnail() {
		return MimeTypeSupport.YES.equals(thumbnail);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MimeTypeSupport getPreview() {
		return preview;
	}

	public void setPreview(MimeTypeSupport preview) {
		this.preview = preview;
	}

	public MimeTypeSupport getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(MimeTypeSupport thumbnail) {
		this.thumbnail = thumbnail;
	}

}
