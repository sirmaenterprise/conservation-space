package com.sirmaenterprise.sep.eai.spreadsheet.service.adapter;

import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.RETRIEVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.ContextualWrapper;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest;

/**
 * Tests for {@link LocalContentIntegrateObjectsServiceAdapter}.
 * 
 * @author gshevkedov
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalContentIntegrateObjectsServiceAdapterTest {
	@Mock
	private EAICommunicationService communicationService;
	@Mock
	private InstanceContentService contentService;
	@Mock
	private DomainInstanceService instanceService;
	@Mock
	private EAIRequestProvider requestProvider;
	@Mock
	private InstanceTypeResolver resolver;
	@Mock
	private EAIResponseReader responseReader;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private TransactionSupportFake transactionSupport;
	@Mock
	private SpreadsheetWriter writer;
	@Mock
	private InstanceContextInitializer contextInitializer;
	@Mock
	private InstanceOperations instanceOperations;
	@Mock
	private SpreadsheetIntegrationConfiguration spreadsheetConfiguration;
	@Mock
	private TaskExecutorFake taskExecutor;
	@InjectMocks
	private LocalContentIntegrateObjectsServiceAdapter localContentIntegrateObjectsServiceAdapter;

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#importInTransaction(com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest, com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances)}
	 * .
	 */
	@Test
	public void testImportInTransaction() throws Exception {
		doCallRealMethod().when(transactionSupport).invokeInNewTx(any(Callable.class));
		doCallRealMethod().when(taskExecutor).submit(any(Supplier.class));
		when(spreadsheetConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<Integer>(1));
		RequestInfo requestInfo = mock(RequestInfo.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(RETRIEVE), any())).thenReturn(requestInfo);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		when(communicationService.invoke(eq(requestInfo))).thenReturn(responseInfo);
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);
		when(responseReader.parseResponse(eq(responseInfo))).thenReturn(processed);
		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);

		InstanceReference reportReference = mock(InstanceReference.class);

		List<ParsedInstance> importable = new ArrayList<>();
		ParsedInstance parsedInstance1 = mock(ParsedInstance.class);
		SpreadsheetEntryId entryId1 = mock(SpreadsheetEntryId.class);
		when(parsedInstance1.getExternalId()).thenReturn(entryId1);
		importable.add(parsedInstance1);
		Instance instance1 = mock(Instance.class);
		InstanceType instanceTypeVal = mock(InstanceType.class);
		Optional<InstanceType> instanceType = Optional.of(instanceTypeVal);
		when(instance1.getId()).thenReturn("emf:1");
		when(resolver.resolve(eq("emf:1"))).thenReturn(instanceType);

		when(parsedInstance1.getParsed()).thenReturn(instance1);
		ParsedInstance parsedInstance2 = mock(ParsedInstance.class);
		SpreadsheetEntryId entryId2 = mock(SpreadsheetEntryId.class);
		when(parsedInstance2.getExternalId()).thenReturn(entryId2);
		importable.add(parsedInstance2);
		Instance instance2 = mock(Instance.class);
		when(parsedInstance2.getParsed()).thenReturn(instance2);
		when(instance2.getId()).thenReturn("emf:2");
		when(resolver.resolve(eq("emf:2"))).thenReturn(Optional.empty());

		when(processed.getInstances()).thenReturn(importable);
		when(request.getReport()).thenReturn(reportReference);
		Collection<SpreadsheetEntryId> requested = new ArrayList<>();
		requested.add(parsedInstance1.getExternalId());
		requested.add(parsedInstance2.getExternalId());

		when(request.getRequestData()).thenReturn(requested);
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentService.getContent(eq(reportReference), eq(Content.PRIMARY_CONTENT))).thenReturn(contentInfo);
		when(contentInfo.exists()).thenReturn(true);
		InstanceReference contextReference = mock(InstanceReference.class);
		InstanceReference targetReference = mock(InstanceReference.class);
		when(request.getContext()).thenReturn(contextReference);
		when(request.getTargetReference()).thenReturn(targetReference);
		ContextualExecutor contextualExecutor = mock(ContextualExecutor.class);
		when(securityContextManager.executeAsAdmin()).thenReturn(contextualExecutor);
		ContextualWrapper contextualWrapper = new ContextualWrapper.NoConextualWrapper();
		when(securityContextManager.wrap()).thenReturn(contextualWrapper);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.importInstances(request));
		verify(instanceService, times(2)).save(any());

	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#importInTransaction(com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest, com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances)}
	 * .
	 */
	@Test(expected = EAIException.class)
	public void testImportInTransactionFailed() throws Exception {
		RequestInfo requestInfo = mock(RequestInfo.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(RETRIEVE), any())).thenReturn(requestInfo);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		when(communicationService.invoke(eq(requestInfo))).thenReturn(responseInfo);
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);
		when(responseReader.parseResponse(eq(responseInfo))).thenReturn(processed);
		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);

		when(processed.getError()).thenReturn(new EAIReportableException("Error"));
		InstanceReference report = mock(InstanceReference.class);
		when(request.getReport()).thenReturn(report);
		InstanceReference contextReference = mock(InstanceReference.class);
		InstanceReference targetReference = mock(InstanceReference.class);
		when(request.getContext()).thenReturn(contextReference);
		when(request.getTargetReference()).thenReturn(targetReference);
		localContentIntegrateObjectsServiceAdapter.importInstances(request);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#persistEntries(com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances, java.util.function.Function)}
	 * .
	 */
	@Test
	public void testPersistEntries() throws Exception {
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);
		Function<Callable<Instance>, Instance> function = transactionSupport::invokeInTx;
		ContextualWrapper contextualWrapper = new ContextualWrapper.NoConextualWrapper();
		when(spreadsheetConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<Integer>(1));
		when(securityContextManager.wrap()).thenReturn(contextualWrapper);
		List<ParsedInstance> importable = new ArrayList<>();
		ParsedInstance parsedInstance = mock(ParsedInstance.class);
		importable.add(parsedInstance);
		when(processed.getInstances()).thenReturn(importable);
		Instance instance = mock(Instance.class);
		when(parsedInstance.getParsed()).thenReturn(instance);
		localContentIntegrateObjectsServiceAdapter.persistEntries(processed.getInstances(), function);
		assertNotNull(processed.getInstances());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#persistInstance(com.sirma.itt.seip.domain.instance.Instance)}
	 * .
	 */
	@Test
	public void testPersistInstance() throws Exception {
		Instance instance = mock(Instance.class);
		Instance context = mock(Instance.class);
		ParsedInstance parsedInstance = new ParsedInstance(mock(SpreadsheetEntryId.class), instance,
				mock(SpreadsheetEntry.class), context);
		InstanceReference parentReference = mock(InstanceReference.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceReference);
		when(instanceReference.getParent()).thenReturn(parentReference);
		when(parentReference.getIdentifier()).thenReturn("emf:9999988888");
		when(context.getId()).thenReturn("emf:111222333");
		when(instanceReference.toInstance()).thenReturn(instance);
		InstanceType instanceTypeVal = mock(InstanceType.class);
		Optional<InstanceType> instanceType = Optional.of(instanceTypeVal);
		when(resolver.resolve(anyString())).thenReturn(instanceType);
		when(instance.getId()).thenReturn("emf:123123123");
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#persistInstance(com.sirma.itt.seip.domain.instance.Instance)}
	 * .
	 */
	@Test
	public void testPersistInstanceWithNullContext() throws Exception {
		Instance instance = mock(Instance.class);
		ParsedInstance parsedInstance = new ParsedInstance(mock(SpreadsheetEntryId.class), instance,
				mock(SpreadsheetEntry.class), null);
		InstanceReference parentReference = mock(InstanceReference.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceReference);
		when(instanceReference.getParent()).thenReturn(parentReference);
		when(parentReference.getIdentifier()).thenReturn("emf:9999988888");
		when(instanceReference.toInstance()).thenReturn(instance);
		InstanceType instanceTypeVal = mock(InstanceType.class);
		Optional<InstanceType> instanceType = Optional.of(instanceTypeVal);
		when(resolver.resolve(anyString())).thenReturn(instanceType);
		when(instance.getId()).thenReturn("emf:123123123");
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
	}

	@Test
	public void testPersistInstanceWithNullContextAndNullParent() throws Exception {
		Instance instance = mock(Instance.class);
		Instance context = mock(Instance.class);
		ParsedInstance parsedInstance = new ParsedInstance(mock(SpreadsheetEntryId.class), instance,
				mock(SpreadsheetEntry.class), context);
		InstanceReference parentReference = mock(InstanceReference.class);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceReference);
		when(parentReference.getIdentifier()).thenReturn("emf:9999988888");
		when(context.getId()).thenReturn("emf:111222333");
		when(instanceReference.toInstance()).thenReturn(instance);
		InstanceType instanceTypeVal = mock(InstanceType.class);
		Optional<InstanceType> instanceType = Optional.of(instanceTypeVal);
		when(resolver.resolve(anyString())).thenReturn(instanceType);
		when(instance.getId()).thenReturn("emf:123123123");
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
		verify(contextInitializer, atLeastOnce()).restoreHierarchy(instance);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#getName()}
	 * .
	 */
	@Test
	public void testGetName() throws Exception {
		assertEquals(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID,
				localContentIntegrateObjectsServiceAdapter.getName());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#resolveInstance(com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier, boolean)}
	 * .
	 */
	@Test(expected = EAIException.class)
	public void testResolveInstance() throws Exception {
		localContentIntegrateObjectsServiceAdapter.resolveInstance(mock(ExternalInstanceIdentifier.class), false);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#extractExternalInstanceIdentifier(com.sirma.itt.seip.domain.instance.Instance)}
	 * .
	 */
	@Test
	public void testExtractExternalInstanceIdentifier() throws Exception {
		ExternalInstanceIdentifier extractExternalInstanceIdentifier = localContentIntegrateObjectsServiceAdapter
				.extractExternalInstanceIdentifier(mock(Instance.class));
		assertNull(extractExternalInstanceIdentifier);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.eai.spreadsheet.service.adapter.LocalContentIntegrateObjectsServiceAdapter#isResolveSupported(com.sirma.itt.seip.eai.model.internal.ResolvableInstance)}
	 * .
	 */
	@Test
	public void testIsResolveSupported() throws Exception {
		assertFalse(localContentIntegrateObjectsServiceAdapter.isResolveSupported(mock(ResolvableInstance.class)));
	}

}
