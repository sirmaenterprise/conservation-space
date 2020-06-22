package com.sirmaenterprise.sep.eai.spreadsheet.service;

import java.util.List;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * Define an EAI API for spreadsheet writing. Additional content type plugins could be registered as extensions.
 *
 * @author bbanchev
 */
public interface SpreadsheetWriter extends Plugin {

	/** The target name. */
	String TARGET_NAME = "spreadsheetWriter";

	/**
	 * Parses the entries. Populates the {@link SpreadsheetSheet} with parsed entries
	 *
	 * @param content
	 *            the content as source to parse and append to
	 * @param entries
	 *            the requested entries to
	 * @return the generated {@link Content}
	 * @throws EAIException
	 *             the exception on any failure
	 */
	Content writerEntries(ContentInfo content, List<SpreadsheetEntry> entries) throws EAIException;

	/**
	 * Checks if parsing is supported.
	 *
	 * @param content
	 *            the content to check
	 * @return true, if is supported
	 */
	boolean isSupported(ContentInfo content);

}
