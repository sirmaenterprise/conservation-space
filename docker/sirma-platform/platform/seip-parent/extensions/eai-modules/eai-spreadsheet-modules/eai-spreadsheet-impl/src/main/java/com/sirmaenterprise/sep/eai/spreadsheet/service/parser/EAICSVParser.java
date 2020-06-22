package com.sirmaenterprise.sep.eai.spreadsheet.service.parser;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.IMPORT_STATUS;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.ContentInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;
import com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil;

/**
 * CSV parser for parsing csv rows to spreadsheet entries.
 *
 * @author gshevkedov
 * @author bbanchev
 */
@Singleton
@Extension(target = SpreadsheetParser.TARGET_NAME, order = 25)
public class EAICSVParser implements SpreadsheetParser {

	@Override
	public SpreadsheetSheet parseEntries(ContentInfo content, Map<String, Collection<String>> requestedRows)
			throws EAIException {
		return readCSV(content.getInputStream(), requestedRows);
	}

	@Override
	public boolean isSupported(ContentInfo content) {
		return EAISpreadsheetConstants.CSV_MIMETYPE.equals(content.getMimeType())
				|| EAISpreadsheetConstants.TEXT_MIMETYPE.equals(content.getMimeType());
	}

	private static SpreadsheetSheet readCSV(InputStream inputStream, Map<String, Collection<String>> requestedRows)
			throws EAIException {
		try (Scanner source = new Scanner(inputStream)) {
			return readCSVRows(source, requestedRows);
		}
	}

	private static SpreadsheetSheet readCSVRows(Scanner source, Map<String, Collection<String>> requestedEntries)
			throws EAIException {
		String[] columnHeaders = null;
		SpreadsheetSheet spreadSheet = new SpreadsheetSheet();
		int rowNum = 1;
		String delimiter = "";
		Collection<String> requestedRows = requestedEntries == null ? null
				: requestedEntries.getOrDefault(EAISpreadsheetConstants.DEFAULT_SHEET_ID, Collections.emptyList());
		while (source.hasNextLine()) {
			String csvData = source.nextLine();
			if (rowNum == 1) {
				// first row contains only property URIs, so we can iterate it and find out which delimiter is used
				delimiter = SpreadsheetUtil.getCSVDelimiter(csvData);
				columnHeaders = csvData.split(delimiter);
			} else {
				String rowId = String.valueOf(rowNum);
				if (requestedRows == null || requestedRows.contains(rowId)) {
					spreadSheet.addEntry(processDataRow(columnHeaders, rowId, csvData.split(delimiter)));
				}
			}
			rowNum++;
		}
		return spreadSheet;
	}

	private static SpreadsheetEntry processDataRow(String[] columnHeaders, String rowId, String[] cellValues) {
		SpreadsheetEntry entry = new SpreadsheetEntry(EAISpreadsheetConstants.DEFAULT_SHEET_ID, rowId);
		for (int i = 0; i < cellValues.length; i++) {
			if (StringUtils.isNotBlank(cellValues[i]) && i < columnHeaders.length
			// filter out not relative property
					&& !IMPORT_STATUS.equals(columnHeaders[i])) {
				entry.put(columnHeaders[i], cellValues[i]);
			}
		}
		return entry;
	}
}
