package com.sirma.itt.seip.eai.cs.service;

import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.TYPE_CULTURAL_OBJECT;
import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.TYPE_IMAGE;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.internal.CSResolvableInstance;
import com.sirma.itt.seip.eai.cs.model.response.CSRetrieveItemsResponse;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.RelationInformation;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.model.internal.RetrievedInstances;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirma.itt.seip.eai.service.IntegrateObjectsServiceAdapter;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Generic CS adapter logic of {@link IntegrateObjectsServiceAdapter} extension.
 *
 * @author bbanchev
 */
public abstract class CSIntegrateObjectsServiceAdapter implements IntegrateObjectsServiceAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	protected DomainInstanceService instanceService;
	@Inject
	protected LinkService linkService;
	@Inject
	private DefinitionService definitionService;
	@Inject
	protected IntegrateExternalObjectsService importService;
	@Inject
	protected EAICommunicationService communicationService;
	@Inject
	protected EAIRequestProvider requestProvider;
	@Inject
	protected EAIResponseReader responseReader;
	@Inject
	protected ModelService modelService;
	@Inject
	private InstanceTypeResolver resolver;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private ThumbnailService thumbnailService;

	@Override
	public <T extends ExternalInstanceIdentifier> Collection<Instance> importInstances(Collection<T> externalIds,
			boolean linkInstances, boolean resolveLinks) throws EAIException {
		RequestInfo request = requestProvider.provideRequest(getName(), BaseEAIServices.RETRIEVE, externalIds);
		ResponseInfo response = communicationService.invoke(request);
		EAIException rootCause = null;
		StringBuilder errorMessages = null;
		if (response.getResponse() instanceof CSRetrieveItemsResponse) {
			rootCause = appendAndLogException(rootCause, ((CSRetrieveItemsResponse) response.getResponse()).getError());
			if (rootCause != null) {
				errorMessages = appendErrorMessage(errorMessages, rootCause);
			}
		}
		RetrievedInstances<Instance> parsedResult = null;
		try {
			parsedResult = responseReader.parseResponse(response);
			if (parsedResult.getError() != null) {
				rootCause = appendAndLogException(rootCause, parsedResult.getError());
				errorMessages = appendErrorMessage(errorMessages, parsedResult.getError());
			}
		} catch (EAIException e) {
			rootCause = appendAndLogException(rootCause, e);
			errorMessages = appendErrorMessage(errorMessages, e);
		}
		Collection<Instance> imported = null;
		if (parsedResult != null) {
			// import all instances in a transaction
			// when all data is available and ready to be imported
			ImportData toImport = new ImportData(parsedResult, linkInstances, resolveLinks, rootCause, errorMessages);
			try {
				imported = transactionSupport.invokeInTx(() -> importInTransaction(toImport));
			} catch (Exception e) {
				// the EAIReportableException was already processed in the method importInTransaction(ImportData)
				if (!(toImport.rootCause instanceof EAIReportableException)) {
					LOGGER.error("Failed to process a request!", e);
				}
			} finally {
				// restore any error data from the invocation
				rootCause = toImport.rootCause;
				errorMessages = toImport.errorMessages;
			}
		}
		if (errorMessages != null) {
			// all exceptions should already be logged, so generate a general exception that should not be reported
			throw new EAIException(errorMessages.toString(), rootCause);
		}
		return imported;
	}

	Collection<Instance> importInTransaction(ImportData toImport) throws Exception {
		try {
			return processImportResponse(toImport.importable, toImport.linkInstances, toImport.resolveLink);
		} catch (EAIReportableException e) {
			toImport.rootCause = appendAndLogException(toImport.rootCause, e);
			toImport.errorMessages = appendErrorMessage(toImport.errorMessages, e);
			throw e;
		}
	}

	private static StringBuilder appendErrorMessage(StringBuilder errorMessages, Exception cause) {
		StringBuilder errorMessagesLocal = errorMessages;
		if (errorMessagesLocal == null) {
			errorMessagesLocal = new StringBuilder();
		}
		errorMessagesLocal.append(cause.getMessage()).append("\r\n");
		return errorMessagesLocal;
	}

	private EAIException appendAndLogException(EAIException rootCause, EAIException cause) {
		if (cause == null) {
			return null;
		}
		try {
			RequestInfo request = requestProvider.provideRequest(getName(), BaseEAIServices.LOGGING, cause);
			communicationService.invoke(request);
			LOGGER.error("System {} is notified for the error {}", getName(), cause.getMessage());
		} catch (Exception e) {
			LOGGER.error("Failed to notify external system {}", getName(), e);
		}
		if (rootCause == null) {
			return cause;
		}
		rootCause.addSuppressed(cause);
		return rootCause;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ExternalInstanceIdentifier> T extractExternalInstanceIdentifier(Instance source)
			throws EAIException {
		Map<String, Serializable> properties = source.getProperties();
		Serializable externalSystemId = properties.get(EAIServicesConstants.PROPERTY_INTEGRATED_SYSTEM_ID);
		if (externalSystemId == null || !getName().equalsIgnoreCase(String.valueOf(externalSystemId))) {
			return null;
		}
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(source);
		// cache by uri the properties
		Optional<PropertyDefinition> externalURI = instanceDefinition
				.fieldsStream()
					.filter(e -> StringUtils.isNotBlank(e.getUri())
							&& EAIServicesConstants.URI_EXTERNAL_ID.equalsIgnoreCase(e.getUri()))
					.findFirst();
		if (!externalURI.isPresent()) {
			throw new EAIRuntimeException("Missing required argument external id for instance: " + source);
		}
		Optional<PropertyDefinition> externalSystem = instanceDefinition
				.fieldsStream()
					.filter(e -> StringUtils.isNotBlank(e.getUri())
							&& EAIServicesConstants.URI_SUB_SYSTEM_ID.equalsIgnoreCase(e.getUri()))
					.findFirst();
		if (!externalSystem.isPresent()) {
			throw new EAIRuntimeException("Missing required argument external system id for instance: " + source);
		}
		List<Pair<String, Serializable>> sourceSystemId = modelService
				.provideModelConverter(getName())
					.convertSEIPtoExternalProperty(EAIServicesConstants.URI_SUB_SYSTEM_ID,
							properties.get(externalSystem.get().getName()), instanceDefinition.getIdentifier());
		if (sourceSystemId.isEmpty()) {
			throw new EAIRuntimeException("Missing required argument source system id for instance: " + source);
		}
		return (T) new CSExternalInstanceId(String.valueOf(properties.get(externalURI.get().getName())),
				String.valueOf(sourceSystemId.iterator().next().getSecond()));
	}

	protected Collection<Instance> processImportResponse(RetrievedInstances<Instance> importable, boolean linkInstances,
			boolean resolveLinks) throws EAIException {
		LOGGER.debug("Processing import request {}...", importable);
		List<Instance> instances = importable.getInstances();
		Map<Serializable, Instance> persisted = new HashMap<>(instances.size());
		for (Instance instance : instances) {
			Instance persistInstance = persistInstance(instance);
			if (!persistInstance.getId().equals(instance.getId())) {
				LOGGER.error("Persisted instance {} has different id than expected {}!", persistInstance.getId(),
						instance.getId());
			}
			persisted.put(persistInstance.getId(), persistInstance);
		}
		if (!linkInstances) {
			return persisted.values();
		}
		LOGGER.trace("Going to create relationships on {}!", persisted);
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		for (RelationInformation relation : importable.getRelations()) {
			try {
				createRelationModel(resolveLinks, persisted, relation);
			} catch (EAIReportableException e) {
				errorBuilder.append(e.getMessage()).append(" on ").append(relation).append("\r\n");
				LOGGER.error("Error during relationship creation!", e);
			} catch (Exception e) {
				LOGGER.error("Error during relationship creation!", e);
			}
		}
		if (errorBuilder.hasErrors()) {
			throw new EAIReportableException(
					"Selected object(s) are imported. However some relationships could not be created due to: "
							+ errorBuilder.toString());
		}
		return persisted.values();
	}

	private void createRelationModel(boolean resolveLinks, Map<Serializable, Instance> persisted,
			RelationInformation relation) throws EAIException {
		Instance from = persisted.get(relation.getSourceInstance().getId());
		if (from == null) {
			// CS-1619
			// When the instance is present in the system and only needs to update relations.
			from = relation.getSourceInstance();
		}
		// get existing or just saved
		Instance to = loadInstanceTo(relation.getTargetInstance(), resolveLinks);
		if (to != null) {
			createRelations(from, to, relation.getRelationUri());
			// create the default links listed in specification
			createDefaultRelationModel(from, to, relation.getRelationUri());
		}
	}

	/**
	 * Creating special links when importing CS cultural objects with primary image so that, the image can be used as
	 * thumbnail when imported.
	 *
	 * @param from
	 *            is the source object
	 * @param to
	 *            is the target document
	 * @param relationId
	 *            the relation identifier
	 * @throws EAIReportableException
	 *             on relation creation error
	 */
	protected void createDefaultRelationModel(Instance from, Instance to, String relationId) throws EAIException {
		if (LinkConstants.HAS_PRIMARY_IMAGE.equals(relationId)) {
			createRelations(from, to, LinkConstants.HAS_THUMBNAIL);
			thumbnailService.register(from, to);
		} else if (LinkConstants.IS_PRIMARY_IMAGE_OF.equals(relationId)) {
			createRelations(from, to, LinkConstants.IS_THUMBNAIL_OF);
		}
		// CMF-19171
		Serializable semanticTypeInstanceFrom = from.get(DefaultProperties.SEMANTIC_TYPE);
		Serializable semanticTypeInstanceTo = to.get(DefaultProperties.SEMANTIC_TYPE);
		if (semanticTypeInstanceFrom == null || semanticTypeInstanceTo == null) {
			throw new EAIException("Failed to detect semantic types for instances " + from + "<->" + to);
		}
		List<String> hierarchyFrom = semanticDefinitionService.getHierarchy(semanticTypeInstanceFrom.toString());
		List<String> hierarchyTo = semanticDefinitionService.getHierarchy(semanticTypeInstanceTo.toString());
		// create parent-child for each object-document pair
		if (hierarchyFrom.contains(TYPE_CULTURAL_OBJECT) && hierarchyTo.contains(TYPE_IMAGE)) {
			createRelations(from, to, InstanceContextService.HAS_CHILD_URI, InstanceContextService.TREE_PARENT_TO_CHILD);
		} else if (hierarchyFrom.contains(TYPE_IMAGE) && hierarchyTo.contains(TYPE_CULTURAL_OBJECT)) {
			createRelations(from, to, InstanceContextService.PART_OF_URI, InstanceContextService.HAS_PARENT);
		}
	}

	/**
	 * Link instances with the provided links with no additional behavior added
	 *
	 * @param from
	 *            the source instance
	 * @param to
	 *            the target instance
	 * @param relations
	 *            list of relations. Should not be null
	 */
	protected void createRelations(Instance from, Instance to, String... relations) {
		for (String relation : relations) {
			// link the required by spec relations in both directions.
			linkService.link(from, to, relation, null, Collections.emptyMap());
		}
	}

	/**
	 * Persist the instance and return it. Disables the stale data checks.
	 *
	 * @param instance
	 *            to persist with action {@link ActionTypeConstants#IMPORT}
	 * @return the persisted instance
	 */
	protected Instance persistInstance(Instance instance) {
		if (resolver.resolve(instance.getId()).isPresent()) {
			LOGGER.info("Storing existing instance {} ", instance.getId());
			try {
				Options.DISABLE_STALE_DATA_CHECKS.disable();
				return saveInstanceAndContent(instance);
			} finally {
				Options.DISABLE_STALE_DATA_CHECKS.enable();
			}
		}
		LOGGER.info("Storing new instance {} ", instance.getId());
		return instanceService.save(InstanceSaveContext.create(instance, new Operation(ActionTypeConstants.IMPORT), new Date()));
	}

	private Instance saveInstanceAndContent(Instance instance) {
		if (instance.get(DefaultProperties.CONTENT) == null) {
			ContentInfo currentView = contentService.getContent(instance, Content.PRIMARY_VIEW);
			if (currentView.exists()) {
				try {
					instance.add(DefaultProperties.CONTENT, currentView.asString());
				} catch (IOException e) {
					throw new EAIRuntimeException("Failed to load content of instance: " + instance.getId()
							+ "! Instance would not be saved!", e);
				}
			}
		}
		return instanceService.save(InstanceSaveContext.create(instance, new Operation("updateInt"), new Date()));
	}

	/**
	 * Loads the instance that is the target in desired relation. Might be {@link CSResolvableInstance} or actual
	 * {@link Instance}
	 *
	 * @param to
	 *            the instance to resolve
	 * @param resolveLinks
	 *            whether to resolve {@link CSResolvableInstance} at all
	 * @return the resolved instance or null if resolving failed
	 * @throws EAIException
	 *             on any error
	 */
	protected Instance loadInstanceTo(Object to, boolean resolveLinks) throws EAIException {
		if (to instanceof Instance) {
			return (Instance) to;
		}
		if (!resolveLinks) {
			return null;
		}
		if (to instanceof CSResolvableInstance) {
			CSResolvableInstance relationInformation = (CSResolvableInstance) to;
			Instance resolveInstance = importService.resolveInstance(relationInformation, true);
			if (resolveInstance != null) {
				return resolveInstance;
			}
			throw new EAIException(to + " is resolved as :[" + resolveInstance + "]!");
		}
		throw new EAIException("Unsupported object " + to + " to be resolved!");
	}

	@Override
	public boolean isResolveSupported(ResolvableInstance resolvable) {
		return resolvable instanceof CSResolvableInstance;
	}

	@Override
	public Instance resolveInstance(ExternalInstanceIdentifier externalIdentifier, boolean persist)
			throws EAIException {
		if (!(externalIdentifier instanceof CSExternalInstanceId)
				|| StringUtils.isBlank(externalIdentifier.getExternalId())
				|| StringUtils.isBlank(((CSExternalInstanceId) externalIdentifier).getSourceSystemId())) {
			throw new EAIException(
					"External id is required argument to resolve instance! Recieved id: " + externalIdentifier);
		}
		Collection<Instance> importInstances = importInstances(Collections.singleton(externalIdentifier), persist,
				false);
		if (importInstances.isEmpty()) {
			return null;
		}
		return importInstances.iterator().next();
	}

	/**
	 * Custom object used to perform the import functionality as the code requires to return multiple data from a single
	 * call (the actual data, execution exception and error messages)
	 *
	 * @author BBonev
	 */
	protected class ImportData {
		protected final RetrievedInstances<Instance> importable;
		protected final boolean linkInstances;
		protected final boolean resolveLink;

		protected EAIException rootCause = null;
		protected StringBuilder errorMessages = null;

		/**
		 * Instantiates a new import data.
		 *
		 * @param importable
		 *            the importable
		 * @param linkInstances
		 *            the link instances
		 * @param resolveLink
		 *            the resolve link
		 * @param rootCause
		 *            the root cause
		 * @param errorMessages
		 *            the error messages
		 */
		protected ImportData(RetrievedInstances<Instance> importable, boolean linkInstances, boolean resolveLink,
				EAIException rootCause, StringBuilder errorMessages) {
			this.importable = importable;
			this.linkInstances = linkInstances;
			this.resolveLink = resolveLink;
			this.rootCause = rootCause;
			this.errorMessages = errorMessages;
		}

	}

}
