package com.sirmaenterprise.sep.eai.spreadsheet.service.util;

import static org.junit.Assert.assertEquals;

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

}
