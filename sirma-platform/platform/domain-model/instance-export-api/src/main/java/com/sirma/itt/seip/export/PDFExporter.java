package com.sirma.itt.seip.export;

import java.io.File;
import java.util.concurrent.TimeoutException;

import com.sirma.itt.seip.StringPair;

/**
 * Defines method for exporting given content to pdf file.
 *
 * @author Ivo Rusev
 */
public interface PDFExporter {

	/**
	 * Exports html to a PDF file and streams it back to the client.
	 *
	 * @param bookmarkableUrl
	 *            the bookmarkable url
	 * @param cookies
	 *            are the current cookies
	 * @return the generated file
	 * @throws TimeoutException when export timesouts
	 */
	File export(String bookmarkableUrl, StringPair... cookies) throws TimeoutException;

}
