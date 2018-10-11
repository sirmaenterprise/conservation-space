package com.sirma.sep.content.jms;

/**
 * Properties describing a {@link com.sirma.sep.content.Content} when communicating between systems and remote
 * services.
 *
 * @author Mihail Radkov
 */
public class ContentCommunicationConstants {

	public static final String CONTENT_ID = "contentId";
	public static final String PURPOSE = "purpose";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_EXTENSION = "fileExtension";
	public static final String REMOTE_SOURCE_NAME = "remoteSourceName";

	private ContentCommunicationConstants() {
		// Private constructor to hide utility class.
	}
}
