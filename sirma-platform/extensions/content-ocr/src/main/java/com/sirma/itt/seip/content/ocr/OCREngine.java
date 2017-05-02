package com.sirma.itt.seip.content.ocr;

import java.io.IOException;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin for orc content of documents.
 *
 * @author hlungov
 */
@Documentation("The Interface OCREngine is extension for ocr content plugins")
public interface OCREngine extends Plugin {

	public static final String TARGET_NAME = "OCREngines";

	/**
	 * Checks if current ocr engine could handle the given mimetype.
	 *
	 * @param mimetype
	 *            the mimetype
	 * @return true, if is applicable
	 */
	boolean isApplicable(String mimetype);

	/**
	 * OCR the document and return the text as a string.
	 *
	 * @param mimeType
	 *            the mimetype
	 * @param fileDescriptor
	 *            the fileDescriptor of instance
	 * @return the ocr text as string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	String doOcr(String mimeType, FileDescriptor fileDescriptor) throws IOException;

}