package com.sirma.sep.content.preview.jms;

/**
 * Attributes used for communication with the remove preview service over JMS's {@link javax.jms.Message}.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewMessageAttributes {

	/**
	 * The ID of created in advance {@link com.sirma.sep.content.Content} which will be used to store content preview
	 * after being generated.
	 */
	public static final String CONTENT_PREVIEW_CONTENT_ID = "contentPreviewContentId";

	private ContentPreviewMessageAttributes() {
		// Private constructor to prevent instantiation
	}

}
