package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class SpreadsheetSheetTest {

	@Test
	public void testAddEntry() throws Exception {
		SpreadsheetSheet sheet = new SpreadsheetSheet();
		assertNotNull(sheet.getEntries().size());
		sheet.addEntry(null);
		assertEquals(0, sheet.getEntries().size());
		sheet.addEntry(mock(SpreadsheetEntry.class));
		assertEquals(1, sheet.getEntries().size());
	}
}
