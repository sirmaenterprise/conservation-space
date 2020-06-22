package com.sirma.itt.seip.eai.content.tool.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SpreadsheetEntryIdTest {

	@Test
	public void testEquals() throws Exception {
		SpreadsheetEntryId first = new SpreadsheetEntryId("1", "1");
		SpreadsheetEntryId second = new SpreadsheetEntryId("1", "1");
		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		second = new SpreadsheetEntryId("1", "2");
		assertNotEquals(first, second);
		assertNotEquals(first.hashCode(), second.hashCode());
		first = new SpreadsheetEntryId("1", "1");
		assertNotEquals(second, first);
		assertNotEquals(second.hashCode(), first.hashCode());
		assertFalse(first.equals(second));
		assertFalse(first.equals(null));
		assertTrue(first.equals(first));
		assertFalse(first.equals(""));
	}

	@Test
	public void testValidInstance() throws Exception {
		SpreadsheetEntryId spreadsheetEntryId = new SpreadsheetEntryId("1", "2");
		assertEquals("1", spreadsheetEntryId.getSheetId());
		assertEquals("2", spreadsheetEntryId.getExternalId());
	}

	@Test
	public void testToString() throws Exception {
		SpreadsheetEntryId spreadsheetEntryId = new SpreadsheetEntryId("1", "1");
		assertEquals("Record[id=1, sheet 1]", spreadsheetEntryId.toString());
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidInitId() throws Exception {
		new SpreadsheetEntryId("1", null);
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidInitSheet() throws Exception {
		new SpreadsheetEntryId(null, "1");
	}
}
