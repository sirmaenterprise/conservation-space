package com.sirma.sep.content.preview.mimetype;

/**
 * Possible finite values for a mime type support.
 *
 * @author Mihail Radkov
 */
public enum MimeTypeSupport {
	YES, // Means the mime type is supported for preview or thumbnail
	SELF, // Means the mime type itself is a preview or thumbnail
	NO  // Means the mime type is not supported for preview or thumbnail
}
