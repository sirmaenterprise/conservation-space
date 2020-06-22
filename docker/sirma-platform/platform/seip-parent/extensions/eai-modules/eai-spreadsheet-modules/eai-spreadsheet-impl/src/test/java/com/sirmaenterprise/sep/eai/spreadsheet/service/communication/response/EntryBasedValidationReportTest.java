package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.NEW_LINE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;

public class EntryBasedValidationReportTest {

	@Test
	public void testSetAndAppend() throws Exception {
		EntryBasedValidationReport report = new EntryBasedValidationReport();
		report.setAndAppend(new SpreadsheetEntryId("0", "0"), "error00");
		report.setAndAppend(new SpreadsheetEntryId("0", "1"), "error01");
		report.setAndAppend(new SpreadsheetEntryId("1", "0"), "error10");
		report.setAndAppend(new SpreadsheetEntryId("1", "1"), "error11");
		String expected = "Record[id=0, sheet=0]:" + NEW_LINE + "error00" + NEW_LINE + NEW_LINE
				+ "Record[id=1, sheet=0]:" + NEW_LINE + "error01" + NEW_LINE + NEW_LINE + "Record[id=0, sheet=1]:"
				+ NEW_LINE + "error10" + NEW_LINE + NEW_LINE + "Record[id=1, sheet=1]:" + NEW_LINE + "error11";
		assertEquals(expected, report.toString());
		// assert second build provides the same result
		assertEquals(expected, report.toString());
	}

	@Test
	public void testEmpty() throws Exception {
		EntryBasedValidationReport report = new EntryBasedValidationReport();
		assertEquals("", report.toString());
	}
}
