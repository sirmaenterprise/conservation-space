package com.sirma.itt.seip.eai.content.tool.service.reader;

import org.apache.poi.ss.usermodel.Row;

/**
 * The Class {@link SpreadsheetUtil} holds some basic methods for work with spreadsheets.
 * 
 * @author gshevkedov
 */
public class SpreadsheetUtil {

	private SpreadsheetUtil() {
		// utility class
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
	 * Retrieves the row id based on the provided row id
	 * 
	 * @param row
	 *            is the source parameter index
	 * @return the id in the spreadsheet for this row
	 */
	public static String getRowId(int row) {
		return String.valueOf(row + 1);
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
