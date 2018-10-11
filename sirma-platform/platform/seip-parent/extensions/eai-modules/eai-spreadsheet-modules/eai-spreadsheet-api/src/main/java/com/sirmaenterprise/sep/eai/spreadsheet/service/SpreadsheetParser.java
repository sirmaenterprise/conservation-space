package com.sirmaenterprise.sep.eai.spreadsheet.service;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;

/**
 * Define an EAI API for spreadsheet processing. Additional content type plugins could be registered as extensions.
 *
 * @author bbanchev
 */
public interface SpreadsheetParser extends Plugin {

	/** The target name. */
	String TARGET_NAME = "spreadsheetParser";

	/**
	 * Parses the entries. Populates the {@link SpreadsheetSheet} with parsed entries
	 *
	 * @param content
	 *            the content to parse from
	 * @param requestedEntries
	 *            if null all data in the content is returned, otherwise only the requested ids
	 * @return the spreadsheet sheet
	 * @throws EAIException
	 *             the exception on any failure
	 */
	SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedEntries)
			throws EAIException;

	/**
	 * Checks if parsing is supported.
	 *
	 * @param content
	 *            the content to check
	 * @return true, if is supported
	 */
	boolean isSupported(ContentInfo content);

}
