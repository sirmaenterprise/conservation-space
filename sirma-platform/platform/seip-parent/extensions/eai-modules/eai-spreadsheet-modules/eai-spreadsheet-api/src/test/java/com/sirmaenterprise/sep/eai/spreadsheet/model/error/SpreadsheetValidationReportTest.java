package com.sirmaenterprise.sep.eai.spreadsheet.model.error;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author bbanchev
 */
public class SpreadsheetValidationReportTest {

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport#hasErrors()}.
	 */
	@Test
	public void testHasErrors() throws Exception {
		SpreadsheetValidationReport spreadsheetValidationReport = new SpreadsheetValidationReport();
		assertFalse(spreadsheetValidationReport.hasErrors());
		spreadsheetValidationReport.append("error");
		assertTrue(spreadsheetValidationReport.hasErrors());
		
		spreadsheetValidationReport = new SpreadsheetValidationReport();
		spreadsheetValidationReport.append(SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION);
		assertFalse(spreadsheetValidationReport.hasErrors());
		spreadsheetValidationReport.append("error");
		assertTrue(spreadsheetValidationReport.hasErrors());
	}

}
