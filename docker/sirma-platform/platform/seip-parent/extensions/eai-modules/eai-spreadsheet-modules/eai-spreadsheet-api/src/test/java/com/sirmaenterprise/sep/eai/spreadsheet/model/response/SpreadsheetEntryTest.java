package com.sirmaenterprise.sep.eai.spreadsheet.model.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

	@Test
	public void testEntryBindUnbind() throws Exception {
		SpreadsheetEntry entry = new SpreadsheetEntry("sheet", "id");
		assertNull(entry.getBindings());
		assertNull(entry.getBinding("not_set"));
		entry.bind(null, "query1");
		assertNull(entry.getBindings());
		entry.bind("field1", "query1");
		assertEquals("query1", entry.getBinding("field1"));
		assertNull(entry.getBinding("not_set"));
		entry.bind("field1", "query2");
		assertEquals("query2", entry.getBinding("field1"));
		entry.unbind(null);
		assertEquals(1, entry.getBindings().size());
		entry.unbind("field1");
		assertEquals(0, entry.getBindings().size());
		assertNull(entry.getBinding("field1"));
		assertNull(entry.getBinding(null));
	}

	@Test(expected = NullPointerException.class)
	public void testEntryBindWithError() throws Exception {
		SpreadsheetEntry entry = new SpreadsheetEntry("sheet", "id");
		assertNull(entry.getBindings());
		entry.bind("key1", null);
	}
}
