package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Tests for {@link EAIInstanceContentRest}.
 *
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class EAIInstanceContentRestTest {
	@Mock
	private Actions actions;
	@Spy
	private SecurityContextManagerFake securityContextManager;
	@InjectMocks
	private EAIInstanceContentRest eAIInstanceContentRest;

	@Test
	public void testPreprocessSpreadsheetSpreadSheetReadRequest() throws Exception {
		SpreadSheetReadRequest request = mock(SpreadSheetReadRequest.class);
		SpreadsheetOperationReport report = mock(SpreadsheetOperationReport.class);
		when(actions.callSlowAction(request)).thenReturn(report);
		assertNotNull(eAIInstanceContentRest.preprocessSpreadsheet(request));
	}

	@Test
	public void testPreprocessSpreadsheetSpreadsheetDataIntegrataionRequest() throws Exception {
		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);
		SpreadsheetOperationReport report = mock(SpreadsheetOperationReport.class);
		when(actions.callSlowAction(request)).thenReturn(report);
		assertNotNull(eAIInstanceContentRest.preprocessSpreadsheet(request));
	}

	@Test
	public void testCreateOrUpdateRestEndpoint() throws Exception {
		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);
		SpreadsheetOperationReport report = mock(SpreadsheetOperationReport.class);
		when(actions.callSlowAction(request)).thenReturn(report);
		assertNotNull(eAIInstanceContentRest.preprocessSpreadsheet(request));
	}

	@Test
	public void testUploadNewVersion() {
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
		eAIInstanceContentRest.save(SaveRequest.buildCreateRequest(new EmfInstance()));
		verify(actions).callAction(Matchers.any(SaveRequest.class));
	}
}