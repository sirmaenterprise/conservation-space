package com.sirma.itt.seip.export;

import java.io.File;
import java.util.concurrent.TimeoutException;


/**
 * Defines method for exporting given content to word file.
 *
 * @author Stella D
 */
public interface WordExporter {

	/**
	 * Export idoc content to word.
	 *
	 * @param request
	 *            the request
	 * @return the file
	 * @throws TimeoutException
	 *             the timeout exception
	 */
	File export(ExportWord request);

}
