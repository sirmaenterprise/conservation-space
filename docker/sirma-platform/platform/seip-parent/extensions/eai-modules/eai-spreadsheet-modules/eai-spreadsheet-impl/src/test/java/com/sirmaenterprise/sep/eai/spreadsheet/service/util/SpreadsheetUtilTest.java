package com.sirmaenterprise.sep.eai.spreadsheet.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

/**
 * Tests for {@link SpreadsheetUtil}.
 * 
 * @author gshevkedov
 */
public class SpreadsheetUtilTest {

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.util.SpreadsheetUtil#getCSVDelimiter(java.lang.String)}.
	 */
	@Test
	public void testGetCSVDelimiter() throws Exception {
		String row = "banana, grape, apple, orange";
		assertEquals(",", SpreadsheetUtil.getCSVDelimiter(row));
	}

	@Test
	public void testIsEmptyRow() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try (InputStream is = SpreadsheetUtil.class.getResourceAsStream("FIM-CSV1.xlsx")) {
			Workbook workbook = WorkbookFactory.create(is);
			Sheet sheet = workbook.getSheetAt(0);
			assertFalse(SpreadsheetUtil.isEmptyRow(sheet.getRow(3)));
			assertTrue(SpreadsheetUtil.isEmptyRow(sheet.getRow(5)));
		}
	}

}
