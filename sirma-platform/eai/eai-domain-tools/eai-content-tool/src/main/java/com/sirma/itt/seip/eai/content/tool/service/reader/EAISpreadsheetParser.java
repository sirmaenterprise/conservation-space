package com.sirma.itt.seip.eai.content.tool.service.reader;

import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.eai.content.tool.exception.EAIException;
import com.sirma.itt.seip.eai.content.tool.model.ContentInfo;
import com.sirma.itt.seip.eai.content.tool.model.SpreadsheetSheet;

/**
 * Parses a spreadsheet using the provided content and optionally only parts of the spreadsheet.
 * 
 * @author gshevkedov
 * @author bbanchev
 */
@FunctionalInterface
public interface EAISpreadsheetParser {

	/**
	 * Parses a content and returns the data as {@link SpreadsheetSheet} bean wrapper.
	 * 
	 * @param content
	 *            is the source content. After parsing conteng stream is closed
	 * @param request
	 *            represents map of requested sheet ids and respective rows for each sheet. Might be null to process all
	 *            sheets and rows.
	 * @return the processed {@link SpreadsheetSheet}
	 * @throws EAIException
	 *             on any fatal error during processing.
	 */
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> request)
			throws EAIException;

}
