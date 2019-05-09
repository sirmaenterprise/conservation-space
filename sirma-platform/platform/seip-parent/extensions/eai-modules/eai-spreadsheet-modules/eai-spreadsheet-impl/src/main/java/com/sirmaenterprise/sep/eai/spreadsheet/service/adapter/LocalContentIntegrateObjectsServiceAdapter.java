package com.sirmaenterprise.sep.eai.spreadsheet.service.adapter;

import static com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations.CREATE_OP;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations.UPDATE_OP;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.DataIntegrationRequest;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.service.IntegrateObjectsServiceAdapter;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.eai.service.model.EAIBaseConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.BeforeInstanceMoveEvent;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.PredefinedLockTypes;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.save.AssignContextStep;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetWriter;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest;

/**
 * {@link IntegrateObjectsServiceAdapter} for local content provided as {@link SpreadsheetDataIntegrataionRequest}.
 * After the import all provided instances are persisted. Source content is updated with optional processed data as
 * dbId, etc.
 *
 * @author bbanchev
 */
@Singleton
@Extension(target = IntegrateObjectsServiceAdapter.PLUGIN_ID, order = 5)
public class LocalContentIntegrateObjectsServiceAdapter implements IntegrateObjectsServiceAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String UNSUPPORTED_FUNCTION_MSG = "Unsupported function!";
	private static final String FAILED_TO_PROCESS_MSG = "Failed to process a request!";

	private static final Operation IMPORT_PARTIAL = new Operation("partialImportFile");
	private static final Operation IMPORT_SUCCESS = new Operation("importFile");

	@Inject
	private DomainInstanceService instanceService;
	@Inject
	private EAICommunicationService communicationService;
	@Inject
	private EAIRequestProvider requestProvider;
	@Inject
	private EAIResponseReader responseReader;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SpreadsheetWriter spreadsheetWriter;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private InstanceContextService contextService;
	@Inject
	private TaskExecutor taskExecutor;
	@Inject
	private SpreadsheetIntegrationConfiguration spreadsheetIntegrationConfiguration;
	@Inject
	private DatabaseIdManager databaseIdManager;
	@Inject
	private LockService lockService;
	@Inject
	private EventService eventService;
	@Inject
	private InstancePropertyNameResolver fieldConverter ;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ExternalInstanceIdentifier, R> Collection<R> importInstances(
			DataIntegrationRequest<T> requestData) throws EAIException {
		SpreadsheetDataIntegrataionRequest integrationRequest = (SpreadsheetDataIntegrataionRequest) requestData;
		IntegrationRequestArgument sourceArgument = new IntegrationRequestArgument(
				integrationRequest.getTargetReference(), integrationRequest.getReport(),
				integrationRequest.getContext(), integrationRequest.getRequestData());
		LOGGER.debug("Request for integration {}", sourceArgument);
		// both adapters might throw EAIException
		RequestInfo request = requestProvider.provideRequest(getName(), SpreadsheetEAIServices.RETRIEVE,
				sourceArgument);
		ResponseInfo response = communicationService.invoke(request);
		Throwable rootCause = null;
		StringBuilder errorMessages = null;
		List<ParsedInstance> stored = null;
		try {
			SpreadsheetResultInstances parsedResult = responseReader.parseResponse(response);
			if (parsedResult.getError() != null) {
				// we pass rootCause as null because we set as rootCause the actual error.
				rootCause = appendAndLogException(rootCause, parsedResult.getError());
				errorMessages = appendErrorMessage(errorMessages, parsedResult.getError().getMessage());
			} else {
				int fragments = FragmentedWork.computeBatchForNFragments(parsedResult.getInstances().size(),
						spreadsheetIntegrationConfiguration.getParallelismCount().get().intValue());
				Collection<Future<List<ParsedInstance>>> futures = FragmentedWork.doWorkAndReduce(
						parsedResult.getInstances(), fragments,
						data -> taskExecutor.submit(() -> persistEntries(data, transactionSupport::invokeInNewTx)));
				stored = executeFutureTasks(futures);
				final List<ParsedInstance> result = Collections.unmodifiableList(stored);
				saveIntegrationDetails(integrationRequest, result);
			}
		} catch (Exception e) {
			errorMessages = appendErrorMessage(errorMessages, e.getMessage());
			LOGGER.error(FAILED_TO_PROCESS_MSG, e);
		}

		if (errorMessages != null) {
			throw new EAIException(errorMessages.toString(), rootCause);
		}
		if (stored == null || requestData.getRequestData().size() != stored.size()) {
			throw new EAIException(
					"Import operations has completed with error/s and some files are not imported. Check server log!",
					rootCause);
		}
		return (Collection<R>) stored;
	}

	List<ParsedInstance> persistEntries(Collection<ParsedInstance> processed,
			final Function<Callable<Instance>, Instance> taskFunction) {
		LOGGER.debug("Processing import request {}...", processed);
		return processed.stream().map(parsedInstance -> {
			boolean isLockedByCurrentProcess = false;
			try {
				LOGGER.debug("Processing next importable instance {}", parsedInstance);
				isLockedByCurrentProcess = lockInstance(parsedInstance);
				if (isLockedByCurrentProcess) {
					taskFunction.apply(() -> persistInstance(parsedInstance));
					updateInstanceUri(parsedInstance);
					return parsedInstance;
				}
				return null;
			} catch (Exception e) {
				LOGGER.error("Error during persistence of entry {}. Skipping entry from further processing!",
						parsedInstance, e);
				// in future add custom status and keep reference
				return null;
			} finally {
				unlockInstance(parsedInstance, isLockedByCurrentProcess);
				LOGGER.debug("Finished processing for importable instance {}", parsedInstance);
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private boolean lockInstance(ParsedInstance parsedInstance) {
		InstanceReference reference = parsedInstance.getParsed().toReference();
		// we can't lock instance that is not created and we don't need to. In this case the unlock will do nothing
		if (Operation.isOperationAs(CREATE_OP.getOperation(), parsedInstance.getSaveOperation().getOperation())) {
			return true;
		}

		if (lockService.lockStatus(reference).isLocked()) {
			return false;
		}

		try {
			// if the lock fails, the instance will be skipped for processing and it will be marked as not processed
			// in the result file (ATM we don't have custom statuses, so it will be marked as 'Not imported')
			transactionSupport.invokeInNewTx(() -> lockService.lock(reference, PredefinedLockTypes.SYSTEM.getType()));
			return true;
		} catch (LockException e) {
			LOGGER.info("Instance [{}] will not be imported, because it is locked. {}", parsedInstance, e.getMessage());
			return false;
		}
	}

	private static void updateInstanceUri(ParsedInstance parsedInstance) {
		// the uri of the instance might change after save, for example when creating user and group, the random id is
		// changed to resource specific id
		SpreadsheetEntry spreadsheetEntry = parsedInstance.getSource();
		if (spreadsheetEntry != null) {
			spreadsheetEntry.put(EAISpreadsheetConstants.URI, parsedInstance.getParsed().getId());
		}
	}

	private List<ParsedInstance> executeFutureTasks(Collection<Future<List<ParsedInstance>>> futures) {
		taskExecutor.waitForAll(futures);
		return futures.stream().flatMap(task -> {
			try {
				return task.get().stream();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Error while collecting import task resuslt!", e);
				return Stream.empty();
			}
		}).collect(Collectors.toList());
	}

	/**
	 * Persist the instance and return it. Disables the stale data checks.
	 *
	 * @param parsedInstance the instance to create or update
	 * @return the persisted instance
	 */
	protected Instance persistInstance(ParsedInstance parsedInstance) {
		if (Operation.isOperationAs(UPDATE_OP.getOperation(), parsedInstance.getSaveOperation().getOperation())) {
			return updateInstance(parsedInstance);
		}

		// CMF-28033 - There is a chance specific instance to be deleted, while imported at the same time(updated via
		// import), which causes data inconsistency. To prevent this behaviour we check, if the instance id was
		// registered, which means that it is completely new instance and it is safe to create it. If check shows that
		// the id is not registered -> instance with that id already exist in the system and we need to generate new
		// id in order to create completely new instance instead of overriding the data of the current
		Instance instance = parsedInstance.getParsed();
		Serializable id = instance.getId();
		if (!databaseIdManager.isIdRegistered(id)) {
			instance.setId(databaseIdManager.generate(true));
		}

		LOGGER.info("Storing new instance {} ", id);
		return instanceService.save(InstanceSaveContext.create(instance, CREATE_OP.getOperation()));
	}

	private Instance updateInstance(ParsedInstance parsedInstance) {
		Instance instance = parsedInstance.getParsed();
		LOGGER.info("Storing existing instance {} ", instance.getId());
		try {
			Options.DISABLE_STALE_DATA_CHECKS.disable();
			// load fresh instance to apply the spreadsheet changes on to prevent concurrent modifications of the same
			// instance in parallel treads. Affected issue: CMF-30269
			Instance freshCopy = instanceService.loadInstance(instance.getId().toString());
			persistParent(instance, parsedInstance.getContext());
			boolean changesAppliedSuccessfully = Trackable.transferChanges(instance, freshCopy);
			if (!changesAppliedSuccessfully) {
				freshCopy = instance;
			}
			return instanceService.save(InstanceSaveContext.create(freshCopy, UPDATE_OP.getOperation()));
		} finally {
			Options.DISABLE_STALE_DATA_CHECKS.enable();
		}
	}

	/**
	 * Just need to set the id of the parent, the save of the createdInstance will do the rest.
	 *
	 * @see AssignContextStep
	 */
	private void persistParent(Instance createdInstance, Instance context) {
		if (context == null) {
			return;
		}

		createdInstance.add(InstanceContextService.HAS_PARENT, context.getId(), fieldConverter);

		// audit move operation, if there is a parent change
		// this logic should be removed, if the audit is handled by AssignContextStep
		if (contextService.isContextChanged(createdInstance)) {
			Instance previousContext = getPrevious(createdInstance);
			eventService.fireNextPhase(new BeforeInstanceMoveEvent(createdInstance, previousContext, context));
		}
	}

	private Instance getPrevious(Instance createdInstance) {
		return contextService.getContext(createdInstance).map(InstanceReference::toInstance).orElse(null);
	}

	private void unlockInstance(ParsedInstance parsedInstance, boolean lockedByCurrentProcess) {
		if (lockedByCurrentProcess) {
			transactionSupport.invokeInNewTx(() -> lockService.unlock(parsedInstance.getParsed().toReference()));
		}
	}

	protected List<ParsedInstance> saveIntegrationDetails(SpreadsheetDataIntegrataionRequest requestData,
			List<ParsedInstance> stored) throws EAIException, IOException {
		ContentInfo currentView = contentService.getContent(requestData.getReport(), Content.PRIMARY_CONTENT);
		Operation operation;
		if (currentView.exists()) {
			ErrorBuilderProvider report = new SpreadsheetValidationReport().append(currentView.asString());
			if (report.hasErrors() || requestData.getRequestData().size() != stored.size()) {
				operation = IMPORT_PARTIAL;
			} else {
				operation = IMPORT_SUCCESS;
			}
		} else {
			throw new EAIException(
					"Cound not load import report. Check the provided object: " + requestData.getReport());
		}
		Instance spreadsheetInstance = requestData.getTargetReference().toInstance();
		ContentInfo spreadsheet = contentService.getContent(spreadsheetInstance, Content.PRIMARY_CONTENT);
		Content writtenEntries = spreadsheetWriter.writerEntries(spreadsheet,
				stored.stream().map(ParsedInstance::getSource).collect(Collectors.toList()));
		Executable saveResult = securityContextManager.executeAsAdmin().toWrapper().executable(() -> {
			contentService.saveContent(spreadsheetInstance, writtenEntries);
			instanceService.save(InstanceSaveContext.create(spreadsheetInstance, operation));
		});
		transactionSupport.invokeInNewTx(saveResult);
		return stored;
	}

	private static StringBuilder appendErrorMessage(StringBuilder errorMessages, String message) {
		StringBuilder errorMessagesLocal = errorMessages;
		if (errorMessagesLocal == null) {
			errorMessagesLocal = new StringBuilder();
		}
		errorMessagesLocal.append(message).append(EAIBaseConstants.NEW_LINE);
		return errorMessagesLocal;
	}

	private static Throwable appendAndLogException(Throwable rootCause, Throwable cause) {
		if (cause == null) {
			return null;
		}
		LOGGER.error("Spreadsheet EAI system has encountered error '{}' during integration request!",
				cause.getMessage());
		if (rootCause == null) {
			return cause;
		}
		rootCause.addSuppressed(cause);
		return rootCause;
	}

	@Override
	public String getName() {
		return SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
	}

	@Override
	public <T extends ExternalInstanceIdentifier> Collection<Instance> importInstances(Collection<T> externalIds,
			boolean linkInstances, boolean resolveLinks) throws EAIException {
		throw new EAIException(UNSUPPORTED_FUNCTION_MSG);
	}

	@Override
	public <T extends ExternalInstanceIdentifier> Instance resolveInstance(T resolvable, boolean persist)
			throws EAIException {
		throw new EAIException(UNSUPPORTED_FUNCTION_MSG);
	}

	@Override
	public <T extends ExternalInstanceIdentifier> T extractExternalInstanceIdentifier(Instance source)
			throws EAIException {
		// not supported
		return null;
	}

	@Override
	public boolean isResolveSupported(ResolvableInstance resolvable) {
		return false;
	}

}
