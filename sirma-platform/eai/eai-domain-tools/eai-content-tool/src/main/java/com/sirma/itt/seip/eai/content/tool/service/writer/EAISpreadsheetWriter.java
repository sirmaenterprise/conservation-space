package com.sirma.itt.seip.eai.content.tool.service.writer;

import java.io.File;

import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;

/**
 * Define an EAI API for spreadsheet writing.
 * 
 * @author gshevkedov
 */
@FunctionalInterface
public interface EAISpreadsheetWriter {

	/**
	 * Stores the spreadsheet at the specified location. In case of problem an exception might be thrown.
	 * 
	 * @param spreadsheet
	 *            the content to store. Contains all new data
	 * @param file
	 *            the storage location
	 * @return the file that the spreadsheet is stored as
	 * @throws EAIException
	 *             the exception on any failure
	 */
	File writerEntries(SpreadsheetSheet spreadsheet, File file) throws EAIException;
}
