package com.sirma.sep.content.preview.messaging;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Available attributes in incoming {@link javax.jms.Message}.
 *
 * @author Mihail Radkov
 */
public class ContentMessageAttributes {

	/**
	 * Points to large binary content in {@link javax.jms.Message}.
	 */
	public static final String SAVE_STREAM = "JMS_HQ_SaveStream";
	public static final String JMS_INPUT_STREAM = "JMS_HQ_InputStream";
	public static final String JMS_DELIVERY_COUNT = "JMSXDeliveryCount";

	public static final String MIMETYPE = "mimetype";
	public static final String INSTANCE_ID = "instanceId";
	public static final String CONTENT_ID = "contentId";
	public static final String INSTANCE_VERSION_ID = "instanceVersionId";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_EXTENSION = "fileExtension";
	public static final String REQUEST_ID = "request_id";
	public static final String AUTHENTICATED_USER = "authenticated_user";
	public static final String EFFECTIVE_USER = "effective_user";
	public static final String TENANT_ID = "tenant_id";
	public static final String CONTENT_PREVIEW_CONTENT_ID = "contentPreviewContentId";
	public static final String CONTENT_PREVIEW_VERSION_CONTENT_ID = "contentPreviewVersionContentId";

	/**
	 * Read only collection holding all string attributes present in {@link ContentMessageAttributes}.
	 */
	public static final Collection<String> STRING_ATTRIBUTES;

	static {
		Collection<String> stringAttributes = new HashSet<>(
				Arrays.asList(MIMETYPE, INSTANCE_ID, CONTENT_ID, INSTANCE_VERSION_ID, FILE_NAME, FILE_EXTENSION,
						REQUEST_ID, AUTHENTICATED_USER, EFFECTIVE_USER, TENANT_ID, CONTENT_PREVIEW_CONTENT_ID,
							  CONTENT_PREVIEW_VERSION_CONTENT_ID));
		STRING_ATTRIBUTES = Collections.unmodifiableCollection(stringAttributes);
	}

	private ContentMessageAttributes() {
		// Private constructor to prevent instantiation
	}

}
