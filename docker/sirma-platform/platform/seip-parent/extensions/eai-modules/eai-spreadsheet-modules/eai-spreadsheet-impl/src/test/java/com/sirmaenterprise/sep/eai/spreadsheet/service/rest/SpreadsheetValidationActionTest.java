package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.PREPARE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.exception.EAIUnsupportedContentException;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;

@RunWith(MockitoJUnitRunner.class)
public class SpreadsheetValidationActionTest {
	private static final String DEF_ID = "testdef";
	private static final String INSTANCE_ID = "instanceid";

	@Mock
	private EAICommunicationService communicationService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private SpreadsheetIntegrationConfiguration eaiConfiguration;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private EAIRequestProvider requestProvider;

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private EAIResponseReader responseReader;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@InjectMocks
	private SpreadsheetValidationAction spreadsheetValidationAction;

	@Test
	public void testReadValid() throws Exception {
		when(eaiConfiguration.getReportDefinitionId()).thenReturn(new ConfigurationPropertyMock<String>(DEF_ID));
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(INSTANCE_ID);
		Instance report = mock(Instance.class);
		when(report.getId()).thenReturn("reportid");
		ContentInfo content = mock(ContentInfo.class);
		RequestInfo requestInfo = mock(RequestInfo.class);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		SpreadsheetResultInstances result = mock(SpreadsheetResultInstances.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(PREPARE), any())).thenReturn(requestInfo);
		when(communicationService.invoke(eq(requestInfo))).thenReturn(responseInfo);
		when(responseReader.parseResponse(eq(responseInfo))).thenReturn(result);
		when(domainInstanceService.createInstance(eq(DEF_ID), eq(INSTANCE_ID))).thenReturn(report);
		when(instanceContentService.saveContent(eq(report), any(Content.class))).thenReturn(content);
		SpreadSheetReadRequest readRequest = new SpreadSheetReadRequest();
		InstanceReference targetReference = mock(InstanceReference.class);
		when(targetReference.toInstance()).thenReturn(instance);
		when(targetReference.getId()).thenReturn(INSTANCE_ID);
		readRequest.setTargetReference(targetReference);
		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(securityContextManager.executeAsSystem()).thenReturn(contextualExecutor);
		SpreadsheetOperationReport read = (SpreadsheetOperationReport) spreadsheetValidationAction.perform(readRequest);

		verify(requestProvider).provideRequest(eq(SYSTEM_ID), eq(PREPARE), any());
		verify(communicationService).invoke(eq(requestInfo));
		verify(responseReader).parseResponse(eq(responseInfo));
		verify(domainInstanceService).createInstance(eq(DEF_ID), eq(INSTANCE_ID));
		verify(instanceContentService).saveContent(eq(report), any(Content.class));

		assertNotNull(read.getResult());
		assertNotNull(read.getReport());
	}

	@Test
	public void testReadWithErrorOnPrepare() throws Exception {
		when(eaiConfiguration.getReportDefinitionId()).thenReturn(new ConfigurationPropertyMock<String>(DEF_ID));
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(INSTANCE_ID);
		Instance report = mock(Instance.class);
		when(report.getId()).thenReturn("reportid");
		ContentInfo content = mock(ContentInfo.class);
		RequestInfo requestInfo = mock(RequestInfo.class);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		SpreadsheetResultInstances result = mock(SpreadsheetResultInstances.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(PREPARE), any()))
				.thenThrow(new EAIUnsupportedContentException("not supported"));
		when(communicationService.invoke(eq(requestInfo))).thenReturn(responseInfo);
		when(responseReader.parseResponse(eq(responseInfo))).thenReturn(result);
		when(domainInstanceService.createInstance(eq(DEF_ID), eq(INSTANCE_ID))).thenReturn(report);
		when(instanceContentService.saveContent(eq(report), any(Content.class))).thenReturn(content);
		SpreadSheetReadRequest readRequest = new SpreadSheetReadRequest();
		InstanceReference targetReference = mock(InstanceReference.class);
		when(targetReference.toInstance()).thenReturn(instance);
		when(targetReference.getId()).thenReturn(INSTANCE_ID);
		readRequest.setTargetReference(targetReference);
		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(securityContextManager.executeAsSystem()).thenReturn(contextualExecutor);
		SpreadsheetOperationReport read = spreadsheetValidationAction.read(readRequest);

		verify(requestProvider).provideRequest(eq(SYSTEM_ID), eq(PREPARE), any());
		verify(communicationService, new AtMost(0)).invoke(eq(requestInfo));
		verify(responseReader, new AtMost(0)).parseResponse(eq(responseInfo));
		verify(domainInstanceService).createInstance(eq(DEF_ID), eq(INSTANCE_ID));
		verify(instanceContentService).saveContent(eq(report), any(Content.class));

		assertNotNull(read.getResult());
		assertNull(read.getResult().getInstances());
		assertNotNull(read.getReport());
	}

	@Test
	public void testReadWithErrorOnCommuncation() throws Exception {
		when(eaiConfiguration.getReportDefinitionId()).thenReturn(new ConfigurationPropertyMock<String>(DEF_ID));
		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(securityContextManager.executeAsSystem()).thenReturn(contextualExecutor);
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(INSTANCE_ID);
		Instance report = mock(Instance.class);
		when(report.getId()).thenReturn("reportid");
		when(domainInstanceService.createInstance(eq(DEF_ID), eq(INSTANCE_ID))).thenReturn(report);
		ContentInfo content = mock(ContentInfo.class);
		when(instanceContentService.saveContent(eq(report), any(Content.class))).thenReturn(content);
		RequestInfo requestInfo = mock(RequestInfo.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(PREPARE), any())).thenReturn(requestInfo);

		SpreadSheetReadRequest readRequest = new SpreadSheetReadRequest();
		InstanceReference targetReference = mock(InstanceReference.class);
		when(targetReference.toInstance()).thenReturn(instance);
		when(targetReference.getId()).thenReturn(INSTANCE_ID);
		readRequest.setTargetReference(targetReference);
		when(communicationService.invoke(eq(requestInfo))).thenThrow(EAIReportableException.class);
		SpreadsheetOperationReport read = spreadsheetValidationAction.read(readRequest);
		assertNull(read.getResult().getInstances());
	}

	@Test(expected = EAIRuntimeException.class)
	public void testReadWithUnreportableErrorOnCommuncation() throws Exception {
		when(eaiConfiguration.getReportDefinitionId()).thenReturn(new ConfigurationPropertyMock<String>(DEF_ID));
		ContextualExecutor contextualExecutor = new ContextualExecutor.NoContextualExecutor();
		when(securityContextManager.executeAsSystem()).thenReturn(contextualExecutor);
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(INSTANCE_ID);
		Instance report = mock(Instance.class);
		when(report.getId()).thenReturn("reportid");
		when(domainInstanceService.createInstance(eq(DEF_ID), eq(INSTANCE_ID))).thenReturn(report);
		ContentInfo content = mock(ContentInfo.class);
		when(instanceContentService.saveContent(eq(report), any(Content.class))).thenReturn(content);
		RequestInfo requestInfo = mock(RequestInfo.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(PREPARE), any())).thenReturn(requestInfo);

		SpreadSheetReadRequest readRequest = new SpreadSheetReadRequest();
		InstanceReference targetReference = mock(InstanceReference.class);
		when(targetReference.toInstance()).thenReturn(instance);
		when(targetReference.getId()).thenReturn(INSTANCE_ID);
		readRequest.setTargetReference(targetReference);
		when(communicationService.invoke(eq(requestInfo))).thenThrow(EAIException.class);
		spreadsheetValidationAction.read(readRequest);
	}

	@Test
	public void testGetName() {
		assertEquals(SpreadSheetReadRequest.OPERATION_NAME, spreadsheetValidationAction.getName());
	}
}
