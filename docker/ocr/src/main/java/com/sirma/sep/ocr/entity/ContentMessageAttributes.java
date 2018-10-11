package com.sirma.sep.ocr.entity;

/**
 * Contains attribute keys used in the content ocr queue messages.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
public enum ContentMessageAttributes {

	MIMETYPE("mimetype"),
	INSTANCE_ID("instanceId"),
	INSTANCE_VERSION_ID("instanceVersionId"),
	FILE_NAME("fileName"),
	FILE_EXTENSION("fileExtension"),
	REQUEST_ID_KEY("request_id"),
	AUTHENTICATED_USER_KEY("authenticated_user"),
	EFFECTIVE_USER_KEY("effective_user"),
	TENANT_ID_KEY("tenant_id"),
	OCRED_CONTENT_ID("ocredContentId"),
	OCRED_VERSION_CONTENT_ID("ocredVersionContentId"),
	OCR_LANGUAGE("ocrLanguage");

	private final String text;

	ContentMessageAttributes(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}