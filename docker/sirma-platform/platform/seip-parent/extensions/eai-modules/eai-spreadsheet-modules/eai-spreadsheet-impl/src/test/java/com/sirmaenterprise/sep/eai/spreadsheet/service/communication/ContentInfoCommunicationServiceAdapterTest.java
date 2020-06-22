package com.sirmaenterprise.sep.eai.spreadsheet.service.communication;

import static com.sirma.sep.content.Content.PRIMARY_CONTENT;
import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.PREPARE;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.RETRIEVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetIntegrationServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.ReadRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

@RunWith(MockitoJUnitRunner.class)
public class ContentInfoCommunicationServiceAdapterTest {
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private SpreadsheetParser spreadsheetParser;
	@InjectMocks
	private ContentInfoCommunicationServiceAdapter contentInfoCommunicationServiceAdapter;
	@Mock
	private Instance instance;
	@Mock
	private ContentInfo contentInfo;

	@Test
	public void testInvokeOnSupportedData() throws Exception {
		SpreadsheetSheet spreadsheetSheet = mockSheetResult();
		ServiceResponse serviceResponse = invokeRequest(true);
		assertEquals(spreadsheetSheet, serviceResponse);
	}

	@Test(expected = EAIException.class)
	public void testInvokeOnNotsupportedData() throws Exception {
		SpreadsheetSheet spreadsheetSheet = mockSheetResult();
		ServiceResponse serviceResponse = invokeRequest(false);
		assertEquals(spreadsheetSheet, serviceResponse);
	}

	@Test
	public void testInvokeRETRIEVEOnSupportedData() throws Exception {
		InstanceReference instanceReference = mock(InstanceReference.class);
		IntegrationRequestArgument sourceArgument = mock(IntegrationRequestArgument.class);
		when(sourceArgument.getSource()).thenReturn(instanceReference);
		when(instanceReference.getId()).thenReturn("id");
		SpreadsheetIntegrationServiceRequest request = new SpreadsheetIntegrationServiceRequest(sourceArgument);
		RequestInfo requestInfo = new RequestInfo(contentInfoCommunicationServiceAdapter.getName(), RETRIEVE, request);
		Map<String, Collection<String>> map = new HashMap<>();
		when(spreadsheetParser.parseEntries(contentInfo, map)).thenReturn(Mockito.mock(SpreadsheetSheet.class));
		when(instanceContentService.getContent(eq("id"), eq(PRIMARY_CONTENT))).thenReturn(contentInfo);
		when(spreadsheetParser.isSupported(eq(contentInfo))).thenReturn(true);
		assertNotNull(contentInfoCommunicationServiceAdapter.invoke(requestInfo));
	}

	private ServiceResponse invokeRequest(boolean supported) throws EAIException {
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.getId()).thenReturn("id");
		when(instance.toReference()).thenReturn(instanceReference);
		SpreadsheetReadServiceRequest request = new SpreadsheetReadServiceRequest(
				new ReadRequestArgument(instanceReference, instanceReference));
		RequestInfo requestInfo = new RequestInfo(contentInfoCommunicationServiceAdapter.getName(), PREPARE, request);
		when(instanceContentService.getContent(eq("id"), eq(PRIMARY_CONTENT))).thenReturn(contentInfo);
		when(spreadsheetParser.isSupported(eq(contentInfo))).thenReturn(supported);
		return contentInfoCommunicationServiceAdapter.invoke(requestInfo);

	}

	private SpreadsheetSheet mockSheetResult() throws EAIException {
		SpreadsheetSheet spreadsheetSheet = mock(SpreadsheetSheet.class);
		when(spreadsheetParser.parseEntries(eq(contentInfo), isNull(Map.class))).thenReturn(spreadsheetSheet);
		return spreadsheetSheet;
	}

	@Test
	public void testGetName() throws Exception {
		assertEquals(SYSTEM_ID, contentInfoCommunicationServiceAdapter.getName());
	}

}
