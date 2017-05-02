package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SpreadsheetEntryTest {

	@Test
	public void testEntry() throws Exception {
		SpreadsheetEntry entry = new SpreadsheetEntry("sheet", "id");
		assertNotNull(entry.getProperties());
		assertEquals("id", entry.getExternalId());
		assertEquals("sheet", entry.getSheet());
		entry.getProperties().put("key", "val");
		assertEquals(1, entry.getProperties().size());
		entry.getProperties().put("key", "val2");
		assertEquals(1, entry.getProperties().size());
	}

}
