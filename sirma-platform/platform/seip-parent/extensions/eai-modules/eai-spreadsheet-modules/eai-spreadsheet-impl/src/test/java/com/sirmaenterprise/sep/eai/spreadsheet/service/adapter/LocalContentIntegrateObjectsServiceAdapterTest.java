package com.sirmaenterprise.sep.eai.spreadsheet.service.adapter;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices.RETRIEVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.verification.AtLeast;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.BeforeInstanceMoveEvent;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.PredefinedLockTypes;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations;
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
	private DomainInstanceService instanceService;
	@Mock
	private EAICommunicationService communicationService;
	@Mock
	private EAIRequestProvider requestProvider;
	@Mock
	private EAIResponseReader responseReader;
	@Mock
	private InstanceContentService contentService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private SpreadsheetWriter spreadsheetWriter;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();
	@Mock
	private SpreadsheetIntegrationConfiguration spreadsheetIntegrationConfiguration;
	@InjectMocks
	private LocalContentIntegrateObjectsServiceAdapter localContentIntegrateObjectsServiceAdapter;
	@Mock
	private DatabaseIdManager databaseIdManager;
	@Mock
	private LockService lockService;
	@Mock
	private InstanceContextService contextService;
	@Mock
	private EventService eventService;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
		when(lockService.lock(any(InstanceReference.class), eq(PredefinedLockTypes.SYSTEM.getType())))
				.thenReturn(new LockInfo());
	}

	@Test(expected = EAIException.class)
	public void importInTransactionFailed() throws Exception {
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

	@Test(expected = EAIException.class)
	public void importInTransactionFailedForEntry() throws Exception {
		Instance failingInstance = mockInstance();
		Instance validInstance = mockInstance();
		SpreadsheetDataIntegrataionRequest request = setUpImportRequest("error",
				createParsedInstance(failingInstance, null, IntegrationOperations.CREATE_OP.getOperation()),
				createParsedInstance(validInstance, null, IntegrationOperations.CREATE_OP.getOperation()));
		when(instanceService.save(any(InstanceSaveContext.class))).thenAnswer(invocation -> {
			Instance instance = invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance();
			if (failingInstance.equals(instance)) {
				throw new EmfException();
			}
			return instance;
		});
		localContentIntegrateObjectsServiceAdapter.importInstances(request);
	}

	@Test
	public void importInTransactionSuccess() throws Exception {
		Instance validInstance1 = mockInstance();
		Instance validInstance2 = mockInstance();
		Instance validInstance1Context = mockInstance();

		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION,
				createParsedInstance(validInstance1, validInstance1Context,
						IntegrationOperations.CREATE_OP.getOperation()),
				createParsedInstance(validInstance2, null, IntegrationOperations.CREATE_OP.getOperation()));

		when(instanceService.save(any(InstanceSaveContext.class)))
				.thenAnswer(invocation -> invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance());
		Collection<Object> importInstances = localContentIntegrateObjectsServiceAdapter.importInstances(request);
		assertEquals(2, importInstances.size());
		ArgumentCaptor<InstanceSaveContext> context = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(instanceService, new AtLeast(3)).save(context.capture());
		assertEquals("importFile", context.getValue().getOperation().getOperation());
	}

	@Test
	// for CMF-28033
	public void importInstances_deleteWhileImport_newInstanceIds() throws Exception {
		Instance validInstance1 = mockInstance();
		Instance validInstance2 = mockInstance();
		Instance contextInstance = mockInstance();

		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION,
				createParsedInstance(validInstance1, contextInstance, IntegrationOperations.CREATE_OP.getOperation()),
				createParsedInstance(validInstance2, null, IntegrationOperations.CREATE_OP.getOperation()));

		Serializable instance1Id = validInstance1.getId();
		when(databaseIdManager.isIdRegistered(instance1Id)).thenReturn(Boolean.FALSE);

		Serializable instance2Id = validInstance2.getId();
		when(databaseIdManager.isIdRegistered(instance2Id)).thenReturn(Boolean.TRUE);

		when(databaseIdManager.generate(anyBoolean())).thenReturn(UUID.randomUUID().toString());
		when(instanceService.save(any(InstanceSaveContext.class)))
				.thenAnswer(invocation -> invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance());

		Collection<ParsedInstance> importInstances = localContentIntegrateObjectsServiceAdapter
				.importInstances(request);

		assertEquals(2, importInstances.size());
		ArgumentCaptor<InstanceSaveContext> context = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(instanceService, new AtLeast(3)).save(context.capture());
		assertEquals("importFile", context.getValue().getOperation().getOperation());
		verify(databaseIdManager).generate(true);
		Iterator<ParsedInstance> iterator = importInstances.iterator();
		assertNotEquals(instance1Id, iterator.next().getParsed().getId());
		assertEquals(instance2Id, iterator.next().getParsed().getId());
	}

	@Test
	public void importInTransaction() throws Exception {
		ParsedInstance parsedInstance1 = mock(ParsedInstance.class);
		SpreadsheetEntryId entryId1 = mock(SpreadsheetEntryId.class);
		when(parsedInstance1.getExternalId()).thenReturn(entryId1);
		Instance instance1 = new EmfInstance("emf:1");
		when(parsedInstance1.getParsed()).thenReturn(instance1);
		when(parsedInstance1.getSaveOperation()).thenReturn(IntegrationOperations.CREATE_OP.getOperation());

		ParsedInstance parsedInstance2 = mock(ParsedInstance.class);
		SpreadsheetEntryId entryId2 = mock(SpreadsheetEntryId.class);
		when(parsedInstance2.getExternalId()).thenReturn(entryId2);
		Instance instance2 = new EmfInstance("emf:2");
		when(parsedInstance2.getParsed()).thenReturn(instance2);
		when(parsedInstance2.getSaveOperation()).thenReturn(IntegrationOperations.CREATE_OP.getOperation());

		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION, parsedInstance1, parsedInstance2);

		assertNotNull(localContentIntegrateObjectsServiceAdapter.importInstances(request));
		verify(instanceService, times(3)).save(any());

	}

	private SpreadsheetDataIntegrataionRequest setUpImportRequest(String validation, ParsedInstance... instances)
			throws EAIException, IOException {
		when(spreadsheetIntegrationConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<>(1));
		RequestInfo requestInfo = mock(RequestInfo.class);
		when(requestProvider.provideRequest(eq(SYSTEM_ID), eq(RETRIEVE), any())).thenReturn(requestInfo);
		ResponseInfo responseInfo = mock(ResponseInfo.class);
		when(communicationService.invoke(eq(requestInfo))).thenReturn(responseInfo);
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);
		when(processed.getInstances()).thenReturn(Arrays.asList(instances));
		when(responseReader.parseResponse(eq(responseInfo))).thenReturn(processed);
		SpreadsheetDataIntegrataionRequest request = mock(SpreadsheetDataIntegrataionRequest.class);

		InstanceReference report = mock(InstanceReference.class);
		when(request.getReport()).thenReturn(report);
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.asString()).thenReturn(validation);
		when(contentService.getContent(eq(report), eq(Content.PRIMARY_CONTENT))).thenReturn(info);
		InstanceReference contextReference = mock(InstanceReference.class);
		InstanceReference spreadsheetReference = mock(InstanceReference.class);
		Instance spreadsheetInstance = mockInstance();
		when(spreadsheetReference.toInstance()).thenReturn(spreadsheetInstance);
		when(request.getContext()).thenReturn(contextReference);
		when(request.getTargetReference()).thenReturn(spreadsheetReference);
		Collection<SpreadsheetEntryId> requested = new ArrayList<>();
		for (ParsedInstance parsed : instances) {
			requested.add(parsed.getExternalId());
		}
		when(request.getRequestData()).thenReturn(requested);
		return request;
	}

	private static Instance mockInstance() {
		return new EmfInstance("emf:" + UUID.randomUUID());
	}

	@Test
	public void persistEntries() throws Exception {
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);
		Function<Callable<Instance>, Instance> function = transactionSupport::invokeInTx;
		when(spreadsheetIntegrationConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<>(1));
		List<ParsedInstance> importable = new ArrayList<>();
		ParsedInstance parsedInstance = mock(ParsedInstance.class);
		importable.add(parsedInstance);
		when(processed.getInstances()).thenReturn(importable);
		Instance instance = mockInstance();
		when(parsedInstance.getParsed()).thenReturn(instance);
		localContentIntegrateObjectsServiceAdapter.persistEntries(processed.getInstances(), function);
		assertNotNull(processed.getInstances());
	}

	@Test
	public void should_UpdateUriInSpreadsheetEntry() throws Exception {
		SpreadsheetResultInstances processed = mock(SpreadsheetResultInstances.class);

		when(spreadsheetIntegrationConfiguration.getParallelismCount()).thenReturn(new ConfigurationPropertyMock<>(1));

		List<ParsedInstance> importable = new ArrayList<>();
		ParsedInstance parsedInstance = createParsedInstance("emf:instance-id",
				IntegrationOperations.CREATE_OP.getOperation());
		importable.add(parsedInstance);

		when(processed.getInstances()).thenReturn(importable);
		when(instanceService.save(any(InstanceSaveContext.class))).then(invocation -> {
			InstanceSaveContext instanceSaveContext = invocation.getArgumentAt(0, InstanceSaveContext.class);
			Instance instance = instanceSaveContext.getInstance();
			instance.setId("emf:user");
			return instance;
		});

		localContentIntegrateObjectsServiceAdapter.persistEntries(processed.getInstances(),
				transactionSupport::invokeInTx);

		assertEquals("emf:user", parsedInstance.getSource().getProperties().get(EAISpreadsheetConstants.URI));
	}

	private static ParsedInstance createParsedInstance(String instanceId, Operation operation) {
		SpreadsheetEntry spreadsheetEntry = new SpreadsheetEntry(SYSTEM_ID, SYSTEM_ID);
		spreadsheetEntry.put(EAISpreadsheetConstants.URI, instanceId);
		ParsedInstance parsedInstance = new ParsedInstance(new SpreadsheetEntryId(), new EmfInstance(instanceId),
				spreadsheetEntry, null, operation);
		return parsedInstance;
	}

	@Test
	public void persistInstance() throws Exception {
		Instance instance = mockInstance();
		Instance context = mockInstance();
		ParsedInstance parsedInstance = createParsedInstance(instance, context,
				IntegrationOperations.CREATE_OP.getOperation());
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
	}

	@Test
	public void persistInstanceWithNullContext() throws Exception {
		Instance instance = new EmfInstance("emf:123123123");
		ParsedInstance parsedInstance = createParsedInstance(instance, null,
				IntegrationOperations.CREATE_OP.getOperation());
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
		assertNull(instance.get(InstanceContextService.HAS_PARENT));
		verifyZeroInteractions(contextService, eventService);
	}

	@Test
	public void persistInstanceWithNullContextAndNullParent() throws Exception {
		Instance instance = new EmfInstance("emf:123123123");
		Instance context = new EmfInstance("emf:111222333");
		ParsedInstance parsedInstance = createParsedInstance(instance, context,
				IntegrationOperations.UPDATE_OP.getOperation());
		when(contextService.isContextChanged(instance)).thenReturn(Boolean.TRUE);
		when(contextService.getContext(instance)).thenReturn(Optional.of(InstanceReferenceMock.createGeneric(context)));
		when(instanceService.save(any(InstanceSaveContext.class))).thenReturn(instance);
		assertNotNull(localContentIntegrateObjectsServiceAdapter.persistInstance(parsedInstance));
		assertEquals("emf:111222333", instance.get(InstanceContextService.HAS_PARENT));
		verify(eventService).fireNextPhase(any(BeforeInstanceMoveEvent.class));
	}

	private static ParsedInstance createParsedInstance(Instance instance, Instance context, Operation operation) {
		return new ParsedInstance(mock(SpreadsheetEntryId.class), instance, mock(SpreadsheetEntry.class), context,
				operation);
	}

	@Test
	public void getName() throws Exception {
		assertEquals(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID,
				localContentIntegrateObjectsServiceAdapter.getName());
	}

	@Test(expected = EAIException.class)
	public void resolveInstance() throws Exception {
		localContentIntegrateObjectsServiceAdapter.resolveInstance(mock(ExternalInstanceIdentifier.class), false);
	}

	@Test
	public void extractExternalInstanceIdentifier() throws Exception {
		ExternalInstanceIdentifier extractExternalInstanceIdentifier = localContentIntegrateObjectsServiceAdapter
				.extractExternalInstanceIdentifier(mockInstance());
		assertNull(extractExternalInstanceIdentifier);
	}

	@Test
	public void isResolveSupported() throws Exception {
		assertFalse(localContentIntegrateObjectsServiceAdapter.isResolveSupported(mock(ResolvableInstance.class)));
	}

	@Test(expected = EAIException.class)
	public void updateExistingInstances_lockedInstances() throws Exception {
		when(lockService.lockStatus(any(InstanceReference.class)))
				.thenReturn(new LockInfo(null, "some-user", new Date(), null, user -> false));
		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION,
				createParsedInstance(mockInstance(), null, IntegrationOperations.UPDATE_OP.getOperation()));
		Collection<Object> importInstances = emptyList();
		try {
			when(instanceService.save(any(InstanceSaveContext.class)))
					.thenAnswer(invocation -> invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance());
			importInstances = localContentIntegrateObjectsServiceAdapter.importInstances(request);
		} finally {
			assertTrue(importInstances.isEmpty());
			// called only 1 time to save the report
			verify(instanceService, times(1)).save(any());
			verify(lockService, never()).unlock(any(InstanceReference.class));
		}
	}

	@Test(expected = EAIException.class)
	public void updateExistingInstances_lockFailed() throws Exception {
		Instance instance = mockInstance();
		InstanceReference reference = instance.toReference();
		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION,
				createParsedInstance(instance, null, IntegrationOperations.UPDATE_OP.getOperation()));
		when(lockService.lockStatus(eq(reference))).thenReturn(new LockInfo());
		when(lockService.lock(eq(reference), eq(PredefinedLockTypes.SYSTEM.getType())))
				.thenThrow(new LockException(null, ""));
		Collection<Object> importInstances = emptyList();
		try {
			when(instanceService.save(any(InstanceSaveContext.class)))
					.thenAnswer(invocation -> invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance());
			importInstances = localContentIntegrateObjectsServiceAdapter.importInstances(request);
		} finally {
			assertTrue(importInstances.isEmpty());
			verify(lockService).lock(reference, PredefinedLockTypes.SYSTEM.getType());
			verify(lockService, never()).unlock(any(InstanceReference.class));
			// called only 1 time to save the report
			verify(instanceService, times(1)).save(any());
		}
	}

	@Test
	public void updateExistingInstances_lockSuccessful_updateCompleted() throws Exception {
		Instance instance = mockInstance();
		InstanceReference reference = instance.toReference();
		SpreadsheetDataIntegrataionRequest request = setUpImportRequest(
				SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION,
				createParsedInstance(instance, null, IntegrationOperations.UPDATE_OP.getOperation()));
		when(lockService.lockStatus(eq(reference))).thenReturn(new LockInfo());
		Collection<Object> importInstances = emptyList();
		try {
			when(instanceService.save(any(InstanceSaveContext.class)))
					.thenAnswer(invocation -> invocation.getArgumentAt(0, InstanceSaveContext.class).getInstance());
			importInstances = localContentIntegrateObjectsServiceAdapter.importInstances(request);
		} finally {
			assertEquals(1, importInstances.size());
			verify(lockService).lock(reference, PredefinedLockTypes.SYSTEM.getType());
			verify(lockService).unlock(reference);
			// 1 time for the report save and 1 for the imported instance update
			verify(instanceService, times(2)).save(any());
		}
	}
}
