package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Tests for {@link EAIInstanceContentRest}.
 * 
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIInstanceContentRestTest {
	@Mock
	private Actions actions;
	@InjectMocks
	private EAIInstanceContentRest eAIInstanceContentRest;

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.EAIInstanceContentRest#preprocessSpreadsheet(com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadSheetReadRequest)}
	 * .
	 */
	@Test
	public void testPreprocessSpreadsheetSpreadSheetReadRequest() throws Exception {
		SpreadSheetReadRequest request = Mockito.mock(SpreadSheetReadRequest.class);
		SpreadsheetOperationReport report = Mockito.mock(SpreadsheetOperationReport.class);
		Mockito.when(actions.callAction(request)).thenReturn(report);
		assertNotNull(eAIInstanceContentRest.preprocessSpreadsheet(request));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.rest.EAIInstanceContentRest#preprocessSpreadsheet(com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest)}
	 * .
	 */
	@Test
	public void testPreprocessSpreadsheetSpreadsheetDataIntegrataionRequest() throws Exception {
		SpreadsheetDataIntegrataionRequest request = Mockito.mock(SpreadsheetDataIntegrataionRequest.class);
		SpreadsheetOperationReport report = Mockito.mock(SpreadsheetOperationReport.class);
		Mockito.when(actions.callAction(request)).thenReturn(report);
		assertNotNull(eAIInstanceContentRest.preprocessSpreadsheet(request));
	}

}
