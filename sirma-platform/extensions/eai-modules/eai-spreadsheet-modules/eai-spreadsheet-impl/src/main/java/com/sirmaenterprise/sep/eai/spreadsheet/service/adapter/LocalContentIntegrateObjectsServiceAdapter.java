package com.sirmaenterprise.sep.eai.spreadsheet.service.adapter;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
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
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
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
	private InstanceTypeResolver instanceResolver;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SpreadsheetWriter spreadsheetWriter;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private InstanceContextInitializer contextInitializer;
	@Inject
	private InstanceOperations instanceOperations;
	@Inject
	private TaskExecutor taskExecutor;
	@Inject
	private SpreadsheetIntegrationConfiguration spreadsheetIntegrationConfiguration;

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
				transactionSupport.invokeInNewTx(() -> saveIntegrationDetails(integrationRequest, result));
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
			final Function<Callable<Instance>, Instance> saveFunction) {
		LOGGER.debug("Processing import request {}...", processed);
		return processed.stream().map(parsedInstance -> {
			try {
				saveFunction.apply(() -> persistInstance(parsedInstance));
				return parsedInstance;
			} catch (Exception e) {
				LOGGER.error("Error during persistence of entry {}. Skipping entry from further processing!",
						parsedInstance, e);
				// in future add custom status and keep reference
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private List<ParsedInstance> executeFutureTasks(Collection<Future<List<ParsedInstance>>> futures) {
		taskExecutor.waitForAll(futures);
		List<ParsedInstance> result = new LinkedList<>();
		futures.stream().forEach(task -> {
			try {
				result.addAll(task.get());
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Error while collecting import task resuslt!", e);
			}
		});
		return result;
	}

	/**
	 * Persist the instance and return it. Disables the stale data checks.
	 *
	 * @param parsedInstance
	 *            the instance to create or update
	 * @return the persisted instance
	 */
	protected Instance persistInstance(ParsedInstance parsedInstance) {
		Instance instance = parsedInstance.getParsed();
		Instance context = parsedInstance.getContext();
		if (instanceResolver.resolve(instance.getId()).isPresent()) {
			LOGGER.info("Storing existing instance {} ", instance.getId());
			try {
				Options.DISABLE_STALE_DATA_CHECKS.disable();
				setParent(instance, context);
				return instanceService.save(InstanceSaveContext.create(instance, new Operation(EDIT_DETAILS)));
			} finally {
				Options.DISABLE_STALE_DATA_CHECKS.enable();
			}
		}
		LOGGER.info("Storing new instance {} ", instance.getId());
		return instanceService.save(InstanceSaveContext.create(instance, new Operation(CREATE)));
	}

	private void setParent(Instance createdInstance, Instance context) {
		if (context == null) {
			return;
		}
		InstanceReference reference = createdInstance.toReference();
		if (reference.getParent() == null) {
			contextInitializer.restoreHierarchy(createdInstance);
		}
		if (reference.getParent() != null && !context.getId().equals(reference.getParent().getIdentifier())) {
			instanceOperations.invokeMove(context, new Operation(), createdInstance);
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
		Content writedEntries = spreadsheetWriter.writerEntries(spreadsheet,
				stored.stream().map(ParsedInstance::getSource).collect(Collectors.toList()));
		securityContextManager.executeAsAdmin().executable(() -> {
			contentService.saveContent(spreadsheetInstance, writedEntries);
			instanceService.save(InstanceSaveContext.create(spreadsheetInstance, operation));
		});
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