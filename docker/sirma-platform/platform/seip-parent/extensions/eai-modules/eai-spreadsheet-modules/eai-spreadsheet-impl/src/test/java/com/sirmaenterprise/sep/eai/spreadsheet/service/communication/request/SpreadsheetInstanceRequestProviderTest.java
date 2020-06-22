package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.request;

import static com.sirma.itt.seip.eai.service.communication.BaseEAIServices.DIRECT;
import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.PREPARE;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.RETRIEVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetIntegrationServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.ReadRequestArgument;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetInstanceRequestProviderTest {

	@InjectMocks
	private SpreadsheetInstanceRequestProvider spreadsheetInstanceRequestProvider;

	@Test
	public void testBuildValidReadRequest() throws Exception {
		ReadRequestArgument sourceArgument = mock(ReadRequestArgument.class);
		ServiceRequest buildRequest = spreadsheetInstanceRequestProvider.buildRequest(PREPARE, sourceArgument);
		assertTrue(buildRequest instanceof SpreadsheetReadServiceRequest);
	}

	@Test(expected = EAIException.class)
	public void testBuildInvalidRequest() throws Exception {
		spreadsheetInstanceRequestProvider.buildRequest(DIRECT, null);
	}

	@Test
	public void testBuildValidImportRequest() throws Exception {
		IntegrationRequestArgument sourceArgument = mock(IntegrationRequestArgument.class);
		ServiceRequest buildRequest = spreadsheetInstanceRequestProvider.buildRequest(RETRIEVE, sourceArgument);
		assertTrue(buildRequest instanceof SpreadsheetIntegrationServiceRequest);
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(SYSTEM_ID, spreadsheetInstanceRequestProvider.getName());
	}

}
