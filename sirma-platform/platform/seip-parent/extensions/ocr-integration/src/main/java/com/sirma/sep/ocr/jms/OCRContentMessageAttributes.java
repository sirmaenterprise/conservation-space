package com.sirma.sep.ocr.jms;

/**
 * Contains attribute keys used in the content ocr queues.
 *
 * @author nvelkov
 */
public class OCRContentMessageAttributes {

	public static final String OCRED_CONTENT_ID = "ocredContentId";
	public static final String OCRED_VERSION_CONTENT_ID = "ocredVersionContentId";

	public static final String OCR_LANGUAGE = "ocrLanguage";

	private OCRContentMessageAttributes() {
		// Disallow instantiation.
	}

}
