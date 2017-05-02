/**
 * 
 */
package com.sirmaenterprise.sep.eai.spreadsheet.service.util;

import org.apache.poi.ss.usermodel.Row;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirmaenterprise.sep.eai.spreadsheet.exception.EAIUnsupportedContentException;

/**
 * The Class {@link SpreadsheetUtil} holds some basic methods for work with spreadsheets.
 * 
 * @author gshevkedov
 */
public class SpreadsheetUtil {
	private static final String[] CSV_DELIMITERS = { ",", ";", "\t", "^", "|" };

	private SpreadsheetUtil() {
		// utility class
	}

	/**
	 * Iterates string row and gets csv delimiter.
	 * 
	 * @param csvRow
	 *            the row
	 * @return the delimiter
	 * @throws EAIException
	 */
	public static String getCSVDelimiter(String csvRow) throws EAIException {
		for (int i = 0; i < CSV_DELIMITERS.length; i++) {
			if (csvRow.contains(CSV_DELIMITERS[i])) {
				return CSV_DELIMITERS[i];
			}
		}
		throw new EAIUnsupportedContentException("Unsupported CSV file format!");
	}

	/**
	 * Retrieves the row id based on the provided row
	 * 
	 * @param row
	 *            is the source parameter
	 * @return the id in the spreadsheet for this row
	 */
	public static String getRowId(Row row) {
		return String.valueOf(row.getRowNum() + 1);
	}

	/**
	 * Retrieves the sheet id based on the provided index
	 * 
	 * @param index
	 *            is the source parameter
	 * @return the id for the sheet in the spreadsheet
	 */
	public static String getSheetId(int index) {
		return String.valueOf(index + 1);
	}

	/**
	 * Retrieves the sheet index from provided id
	 * 
	 * @param sheetId
	 *            is the source parameter
	 * @return the index for the sheet in the spreadsheet
	 */
	public static int getSheetIndex(String sheetId) {
		return Integer.parseInt(sheetId) - 1;
	}
}
