package com.sirma.itt.seip.eai.cs.service.communication.response;

import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.NEW_LINE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.db.VirtualDb;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.cs.model.CSItemRecord;
import com.sirma.itt.seip.eai.cs.model.CSItemRelations;
import com.sirma.itt.seip.eai.cs.model.CSResultItem;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.request.CSSearchRequest;
import com.sirma.itt.seip.eai.cs.model.response.CSItemsSetResponse;
import com.sirma.itt.seip.eai.cs.model.response.CSRetrieveItemsResponse;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.URIServiceRequest;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.ExternalInstanceIdentifier;
import com.sirma.itt.seip.eai.model.internal.ProcessedInstanceModel;
import com.sirma.itt.seip.eai.model.internal.RelationInformation;
import com.sirma.itt.seip.eai.model.internal.RetrievedInstances;
import com.sirma.itt.seip.eai.model.internal.SearchResultInstances;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.model.response.StreamingResponse;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.queries.QueryBuildRequest;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * Base CS response adapter implementation that holds reusable parsing methods for all cs partners.
 *
 * @author bbanchev
 */
public abstract class CSResponseReaderAdapter implements EAIResponseReaderAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** prepare import. */
	protected static final Operation PREPARE_IMPORT = new Operation("prepareImport");
	protected static final String EXTERNAL_KEY_SOURCE = "source";
	protected static final String EXTERNAL_KEY_ID = "id";
	@Inject
	protected EAICommunicationService communicationService;
	@Inject
	protected ModelService modelService;
	@Inject
	protected InstanceService instanceService;
	@Inject
	protected DefinitionService definitionService;
	@Inject
	@SemanticDb
	protected InstanceLoader instanceLoader;
	@Inject
	protected SearchService searchService;
	@Inject
	protected QueryBuilder queryBuilder;
	@Inject
	private InstancePropertyNameResolver propertyNameResolver;
	@Inject
	@VirtualDb
	private DbDao dbDao;
	@SuppressWarnings("unchecked")
	@Override
	public <R extends ProcessedInstanceModel> R parseResponse(ResponseInfo response) throws EAIException {
		if (response.getResponse() instanceof CSItemsSetResponse) {
			return (R) parseResultSetResponse(response);
		} else if (response.getResponse() instanceof CSRetrieveItemsResponse) {
			return (R) parseImportItemResponse(response);
		}
		throw new EAIException("Unsupported response received: " + response);
	}

	/**
	 * Parses the {@link CSItemsSetResponse} as {@link SearchResultInstances} with populated paging and instances list.
	 *
	 * @param response
	 *            the response to use as source
	 * @return the populated {@link SearchResultInstances}
	 * @throws EAIException
	 *             on any error or {@link EAIReportableException} on reportable exception
	 */
	protected SearchResultInstances<Instance> parseResultSetResponse(ResponseInfo response) throws EAIException {
		CSItemsSetResponse items = (CSItemsSetResponse) response.getResponse();
		List<CSResultItem> normalized = items.getItems();
		TimeTracker createAndStart = TimeTracker.createAndStart();
		normalizeEntryData(normalized);

		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();

		SearchResultInstances<Instance> parsedInstances = new SearchResultInstances<>();
		parsedInstances.setInstances(new ArrayList<>(normalized.size()));
		Map<CSExternalInstanceId, Instance> existing = loadExistingItems(
				normalized.stream().map(CSResultItem::getRecord).collect(Collectors.toList()), errorBuilder);
		for (CSResultItem item : normalized) {
			CSExternalInstanceId externalId = null;
			CSItemRecord record = item.getRecord();
			String definitionId = null;
			try {
				definitionId = detectDefinitionId(modelService.getModelConfiguration(getName()), record, null);
			} catch (Exception e) {// NOSONAR
				// the error was already logged during load
				continue;
			}
			try {

				externalId = extractExternalId(record, null);
				Instance instance = existing.get(externalId);
				CSSearchRequest req = (CSSearchRequest) response.getRequest();
				if (instance == null && req.isInstantiateMissing()) {
					instance = createImportableInstance(record, definitionId, PREPARE_IMPORT);
					loadThumbnailInInstance(item, instance);
					storeInCache(instance);
				}
				// if instance is loaded or currently created
				if (instance != null) {
					fillRelationsInformation(instance, record);
					parsedInstances.getInstances().add(instance);
				}
			} catch (Exception e) {
				errorLogging(errorBuilder, record, externalId, e);
			}
		}
		if (errorBuilder.hasErrors()) {
			parsedInstances.setError(new EAIReportableException(
					"Error(s) during operation processing:" + NEW_LINE + errorBuilder.get().toString(),
					Objects.toString(items.getItems(), "")));
		}
		parsedInstances.setPaging(items.getPaging());
		LOGGER.debug("Search and processing in {} took {} ms and returned {} instances", getName(),
				Long.valueOf(createAndStart.stop()), Integer.valueOf(parsedInstances.getInstances().size()));
		return parsedInstances;

	}

	/**
	 * Parses the {@link CSRetrieveItemsResponse} and returns a response {@link RetrievedInstances} with instances and
	 * relations to be created.
	 *
	 * @param response
	 *            the response to use as source
	 * @return the populated response with instances and relations to be created
	 * @throws EAIException
	 *             on any error or {@link EAIReportableException} on reportable exception
	 */
	protected RetrievedInstances<Instance> parseImportItemResponse(ResponseInfo response) throws EAIException {

		CSRetrieveItemsResponse items = (CSRetrieveItemsResponse) response.getResponse();

		RetrievedInstances<Instance> parsedInstances = new RetrievedInstances<>();
		parsedInstances.setInstances(new ArrayList<>(items.getRetrieved().size()));
		parsedInstances.setRelations(new LinkedList<>());
		Collection<CSItemRecord> normalized = new LinkedList<>(items.getRetrieved().values());
		for (CSItemRecord csItemRecord : normalized) {
			normalizeEntryDataOnRecord(csItemRecord);
		}

		final ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		// Make a modifiable copy of the map.
		Map<CSExternalInstanceId, Instance> existing = new LinkedHashMap<>(
				iterateAndloadAllExistingItems(normalized, errorBuilder));
		for (CSItemRecord record : normalized) {
			CSExternalInstanceId externalId = null;
			String definitionId = null;
			try {
				definitionId = detectDefinitionId(modelService.getModelConfiguration(getName()), record, null);
			} catch (Exception e) {// NOSONAR
				// the error was already logged
				continue;
			}
			try {
				externalId = extractExternalId(record, null);
				Instance instance = existing.get(externalId);
				if (instance == null && isImportableInstance(record, existing, items.getRetrieved())) {
					instance = createImportableInstance(record, definitionId, PREPARE_IMPORT);
					// add to the cached by id items
					existing.put(externalId, instance);
					appendImportResult(parsedInstances, instance, false, record);
				} else if (instance != null) {
					fillReceivedProperties(instance, record);
					appendImportResult(parsedInstances, instance, true, record);
				}
			} catch (Exception e) {
				errorLogging(errorBuilder, record, externalId, e);
			}
		}
		// iterated second time after update of existing to map to existing instances
		for (CSItemRecord record : normalized) {
			CSExternalInstanceId externalId = extractExternalId(record, null);
			Instance instance = existing.get(externalId);
			if (instance == null) {
				continue;
			}
			try {
				constructRelationInformation(parsedInstances, instance, record, existing,
						modelService.getModelConfigurationByNamespace(getNamespace(record)), errorBuilder);
			} catch (Exception e) {
				errorLogging(errorBuilder, record, externalId, e);
			}
		}

		if (errorBuilder.hasErrors()) {
			parsedInstances.setError(new EAIReportableException(
					"Error(s) during operation processing:" + NEW_LINE + errorBuilder.get().toString(),
					Objects.toString(items.getRetrieved().values(), "")));
		}
		return parsedInstances;
	}

	protected void appendImportResult(RetrievedInstances<Instance> parsedInstances, Instance instance,
			boolean existingInstance, CSItemRecord record) throws EAIException {
		if (instance != null) {
			// add the to list
			parsedInstances.getInstances().add(instance);
		}
	}

	/**
	 * Checks if a given external instance can be imported into the system.
	 * 
	 * @param record
	 *            the record we want to check for import.
	 * @param existingInstances
	 *            the list of existing items to use as reference
	 * @param map
	 *            that contains the allowed records.
	 * @return true if the instance should be imported, false otherwise.
	 * @throws EAIReportableException
	 *             on any error during check
	 */
	protected boolean isImportableInstance(CSItemRecord record, Map<CSExternalInstanceId, Instance> existingInstances,
			Map<CSExternalInstanceId, CSItemRecord> map) throws EAIReportableException {
		return map.containsKey(extractExternalId(record, null));
	}

	private static void errorLogging(ErrorBuilderProvider errorBuilder, CSItemRecord record,
			CSExternalInstanceId externalId, Exception cause) {
		String objectRef = "item";
		if (record != null && StringUtils.isNotBlank(record.getClassification())) {
			objectRef = record.getClassification();
		}
		LOGGER.error("Error on {} for '{}' with details '{}' on record '{}'", objectRef, externalId, cause.getMessage(),
				record, cause);

		errorBuilder
				.get(1024)
					.append("Error on ")
					.append(objectRef)
					.append(" for ")
					.append(externalId)
					.append(" with details: ")
					.append(cause.getMessage())
					.append(NEW_LINE);
	}

	/**
	 * Creates the relations to existing objects by filling information to {@link RetrievedInstances#getRelations()}.
	 *
	 * @param importedInstances
	 *            the result model
	 * @param source
	 *            the source to create relation information for
	 * @param srcRecord
	 *            the record as source of current relations
	 * @param existing
	 *            the map of externalid to loaded instances
	 * @param modelConfiguration
	 *            the cached model
	 * @throws EAIException
	 *             on any error during processing
	 */
	protected void constructRelationInformation(RetrievedInstances<Instance> importedInstances, Instance source,
			CSItemRecord srcRecord, Map<CSExternalInstanceId, Instance> existing, ModelConfiguration modelConfiguration,
			ErrorBuilderProvider errorBuilder) throws EAIException {
		List<CSItemRelations> relationships = srcRecord.getRelationships();
		if (relationships == null) {
			return;
		}
		String externalType = getExternalType(source);
		for (CSItemRelations nextRelation : relationships) {
			Object to = loadRelationAsInformation(srcRecord, nextRelation, existing, errorBuilder);
			if (to == null) {
				// still not imported or not needed - skip it
				continue;
			}
			EntityRelation relation = modelConfiguration.getRelationByExternalName(externalType, nextRelation.getType());
			if (relation == null) {
				errorBuilder
						.append("\r\n Missing relation information: ")
							.append(nextRelation.getType())
							.append(" for ")
							.append(extractExternalId(srcRecord, modelConfiguration));
			} else {
				importedInstances.getRelations().add(new RelationInformation(source, to, relation.getUri()));
			}
		}
	}

	/**
	 * Load relation to information - external id or existing instance.
	 *
	 * @param sourceRecord
	 *            the source record
	 * @param relationInfo
	 *            the relation information
	 * @param existing
	 *            the preloaded existing items to use
	 * @param errorBuilder
	 *            current error tracer for nested errors
	 * @return the {@link RelationInformation}, an existing instance or null if relation should be skipped
	 * @throws EAIException
	 *             on error during information extraction
	 */
	protected abstract Object loadRelationAsInformation(CSItemRecord sourceRecord, CSItemRelations relationInfo,
			Map<CSExternalInstanceId, Instance> existing, ErrorBuilderProvider errorBuilder) throws EAIException;

	/**
	 * Iterate and load all existing items by external id.
	 *
	 * @param normalized
	 *            the normalized data to use as source
	 * @return the map of external id to loaded instance. Might be empty
	 * @throws EAIException
	 *             the exception on process error
	 */
	protected Map<CSExternalInstanceId, Instance> iterateAndloadAllExistingItems(Collection<CSItemRecord> normalized,
			ErrorBuilderProvider errorBuilder) throws EAIException {
		List<CSItemRecord> searchable = new LinkedList<>();
		for (CSItemRecord nextRecord : normalized) {
			searchable.add(nextRecord);
			if (nextRecord.getRelationships() == null) {
				continue;
			}
			for (CSItemRelations nextRelation : nextRecord.getRelationships()) {
				searchable.add(nextRelation.getRecord());
			}
		}
		return loadExistingItems(searchable, errorBuilder);
	}

	/**
	 * Loads the existing items based on the items records and maps the loaded instances to the external id that is
	 * based on.
	 *
	 * @param items
	 *            are the records to search for
	 * @param errorBuilder
	 * @return a map (possibly empty) of externalId to loaded instance.
	 * @throws EAIException
	 *             on error during loading
	 */
	protected Map<CSExternalInstanceId, Instance> loadExistingItems(Collection<CSItemRecord> items,
			ErrorBuilderProvider errorBuilder) throws EAIException {
		if (items.isEmpty()) {
			LOGGER.trace("The result does not contain any data. Skipping search for existing items!");
			return Collections.emptyMap();
		}

		final Map<CSExternalInstanceId, CSItemRecord> externalIds = new LinkedHashMap<>(items.size());
		for (CSItemRecord record : items) {
			CSExternalInstanceId externalId = extractExternalId(record, null);
			if (externalId != null) {
				externalIds.put(externalId, record);
			} else {
				errorBuilder.append("Failed to extract external id for: " + record).append(NEW_LINE);
			}
		}
		List<Instance> result = null;
		try {
			result = executeSearchByRecordInfo(externalIds, errorBuilder);
		} catch (Exception e) {// NOSONAR
			errorBuilder.append(e.getMessage());
		}
		if (result == null || result.isEmpty()) {
			// optimize further processing
			return Collections.emptyMap();
		}
		Map<CSExternalInstanceId, Instance> mappedByExternalId = new LinkedHashMap<>(result.size());
		// better single thread stream
		EAIReportableException loadError = null;
		for (Instance instance : result) {
			try {
				checkAndMapInstanceToExternalId(externalIds, instance, mappedByExternalId);
			} catch (EAIException e) {
				if (loadError == null) {
					loadError = new EAIReportableException("Error(s) during mapping of items to existings items!",
							Objects.toString(externalIds, null));
				}
				loadError.addSuppressed(e);
				errorBuilder.append(e.getMessage()).append(NEW_LINE);
			}
		}
		LOGGER.debug("After mapping to existing items the result is\n {}", mappedByExternalId);
		return mappedByExternalId;
	}

	@SuppressWarnings("unchecked")
	protected List<Instance> executeSearchByRecordInfo(final Map<CSExternalInstanceId, CSItemRecord> externalIds,
			ErrorBuilderProvider errorBuilder) throws EAIException {
		List<Function<String, String>> filters = new ArrayList<>(externalIds.size());
		String externalIdUri = getExternalIdUri(null);
		for (Entry<CSExternalInstanceId, CSItemRecord> externalInstanceId : externalIds.entrySet()) {
			CSExternalInstanceId externalId = externalInstanceId.getKey();
			Function<String, String> externalIdFilter = queryBuilder.buildValueFilter(externalIdUri,
					externalId.getExternalId());
			CSItemRecord item = externalInstanceId.getValue();
			ModelConfiguration modelConfiguration = modelService.getModelConfigurationByNamespace(item.getNamespace());
			EAIModelConverter modelConverter = modelService.provideModelConverterByNamespace(item.getNamespace());
			try {
				String detectDefinitionId = detectDefinitionId(modelConfiguration, item, null);
				Pair<String, Serializable> converted = modelConverter.convertExternaltoSEIPProperty(
						buildNamespacePropertyId(getNamespace(item), EXTERNAL_KEY_SOURCE),
						externalId.getSourceSystemId(), detectDefinitionId);

				if (converted.getSecond() == null) {
					String currentError = "Invalid source system detected: " + externalId.getSourceSystemId() + " for "
							+ externalId;
					errorBuilder.append(currentError).append(NEW_LINE);
					continue;
				}
				if (converted.getFirst() == null) {
					String currentError = "Invalid instance id detected: null";
					errorBuilder.append(currentError).append(NEW_LINE);
					continue;
				}

				Function<String, String> sourceSystemFilter = queryBuilder.buildValueFilter(
						EAIServicesConstants.URI_SUB_SYSTEM_ID, String.valueOf(converted.getSecond()));
				filters.add(queryBuilder.buildComposite(externalIdFilter, sourceSystemFilter));
			} catch (Exception e) {// NOSONAR
				errorBuilder.append(e.getMessage()).append(NEW_LINE);
			}
		}
		if (filters.isEmpty()) {
			return Collections.emptyList();
		}
		QueryBuildRequest buildRequest = new QueryBuildRequest(NamedQueries.DYNAMIC_QUERY)
				.addFilter(NamedQueries.Filters.IS_NOT_DELETED)
					.addFilter(NamedQueries.Filters.IS_NOT_REVISION)
					.addFilter(queryBuilder.buildUnion(CollectionUtils.toArray(filters, Function.class)));

		String searchQuery = queryBuilder.buildQuery(buildRequest);

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setMaxSize(externalIds.size());
		arguments.setPageSize(externalIds.size());
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(searchQuery);
		arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
		searchService.searchAndLoad(Instance.class, arguments);

		List<Instance> result = arguments.getResult();

		LOGGER.debug("For query:\n {} \n\n result is with size {} and value \n {}", searchQuery,
				Integer.valueOf(result.size()), result.stream().map(i -> i.getId()).collect(Collectors.toList()));
		return result;
	}

	private void checkAndMapInstanceToExternalId(
			final Map<CSExternalInstanceId, CSItemRecord> externalIdToExternalRecord, Instance instance,
			Map<CSExternalInstanceId, Instance> externalIdToInternalInstance) throws EAIException {

		// if external type property is not defined then the instance definition will be used for verification
		// we cannot use property name converter here as the new type may not have the defined external type so we
		// need to check for all this here
		String externalType = getExternalType(instance);
		DefinitionModel instanceDefinition = definitionService.find(externalType);
		if (instanceDefinition == null) {
			throw new EAIRuntimeException("Missing s definition for object with id: " + instance.getId());
		}
		// get the record model type
		CSExternalInstanceId recordId = new CSExternalInstanceId(
				String.valueOf(extractExternalIdFromInstance(instance, instanceDefinition)),
				String.valueOf(extractExternalSystemIdFromInstance(instance, instanceDefinition)));
		CSItemRecord csItemRecord = externalIdToExternalRecord.get(recordId);
		EntityType typeByExternalName = modelService
				.getModelConfigurationByNamespace(csItemRecord.getNamespace())
					.getTypeByExternalName(csItemRecord.getClassification());

		if (typeByExternalName == null) {
			throw new EAIReportableException(
					"Type for classification: " + csItemRecord.getClassification() + " not found in model!",
					Objects.toString(csItemRecord, null));
		}

		if (!externalType.equals(typeByExternalName.getIdentifier())) {
			throw new EAIReportableException(
					"The received classification " + typeByExternalName.getIdentifier()
							+ " does not match the classification " + instance.getIdentifier()
							+ " of the existing object with id " + instance.getId(),
					Objects.toString(csItemRecord, null));
		}
		Instance existing = externalIdToInternalInstance.put(recordId, instance);
		if (existing != null) {
			throw new EAIRuntimeException("Duplicate entry with external id: " + recordId);
		}
	}

	private String getExternalType(Instance instance) {
		return instance.getString(EAIServicesConstants.PROPERTY_EXTERNAL_TYPE,
				() -> instance.getString(EAIServicesConstants.URI_EXTERNAL_TYPE, instance.getIdentifier()));
	}

	private Serializable extractExternalIdFromInstance(Instance instance, DefinitionModel instanceDefinition) {
		String externalIdUri = getExternalIdUri(instance.getIdentifier());
		Optional<PropertyDefinition> externalIdProperty = instanceDefinition
				.fieldsStream()
					.filter(e -> externalIdUri.equalsIgnoreCase(e.getUri()))
					.findFirst();
		if (!externalIdProperty.isPresent()) {
			throw new EAIRuntimeException(
					"Could not find property with uri '" + externalIdUri + "' in definition: " + instanceDefinition);
		}
		Serializable externalIdLoaded = instance.getProperties().get(externalIdProperty.get().getIdentifier());
		if (externalIdLoaded == null) {
			throw new EAIRuntimeException(externalIdProperty.get().getIdentifier()
					+ " property found, but is not loaded in instance: " + instance);
		}
		return externalIdLoaded;
	}

	private Serializable extractExternalSystemIdFromInstance(Instance instance, DefinitionModel instanceDefinition) {
		Optional<PropertyDefinition> externalSystemIdProperty = instanceDefinition
				.fieldsStream()
					.filter(e -> EAIServicesConstants.URI_SUB_SYSTEM_ID.equalsIgnoreCase(e.getUri()))
					.findFirst();
		if (!externalSystemIdProperty.isPresent()) {
			throw new EAIRuntimeException("Could not find property with uri '" + EAIServicesConstants.URI_SUB_SYSTEM_ID
					+ "' in definition: " + instanceDefinition);
		}
		Serializable externalIdLoaded = instance.getProperties().get(externalSystemIdProperty.get().getIdentifier());
		if (externalIdLoaded == null) {
			throw new EAIRuntimeException(externalSystemIdProperty.get().getIdentifier()
					+ " property found, but is not loaded in instance: " + instance);
		}
		// convert to external since it is codelist
		List<Pair<String, Serializable>> convertSEIPtoExternalProperty;
		try {
			convertSEIPtoExternalProperty = modelService
					.provideModelConverter(
							instance.getProperties().get(EAIServicesConstants.PROPERTY_INTEGRATED_SYSTEM_ID).toString())
						.convertSEIPtoExternalProperty(EAIServicesConstants.URI_SUB_SYSTEM_ID, externalIdLoaded,
								getExternalType(instance));
		} catch (EAIModelException e) { // NOSONAR
			// rethrow with original cause and message
			throw new EAIRuntimeException(e.getMessage(), e.getCause());
		}
		if (!convertSEIPtoExternalProperty.isEmpty()) {
			return convertSEIPtoExternalProperty.iterator().next().getSecond();
		}
		throw new EAIRuntimeException("Failed to convert property to external value");
	}

	/**
	 * Convert cs item to an instance. The instances is created with operation {@link #PREPARE_IMPORT} to initialize in
	 * correct state. All relations, default data and converted received data are populated in the instances.
	 *
	 * @param record
	 *            the record as source
	 * @param definitionId
	 *            the definition id to use
	 * @param creationOperation
	 *            the operation during instance create
	 * @return the created instance
	 * @throws EAIException
	 *             on any error during data extract or convert
	 */
	protected Instance createImportableInstance(CSItemRecord record, String definitionId, Operation creationOperation)
			throws EAIException {

		DefinitionModel definitionModel = definitionService.find(definitionId);
		if (definitionModel == null) {
			throw new EAIException("Failed to load definition model for: " + definitionId);
		}

		Instance instance = instanceService.createInstance(definitionModel, null, creationOperation);

		fillReceivedProperties(instance, record);
		fillDefaultProperties(instance, record);
		return instance;
	}

	/**
	 * Fill default properties - integrated system id and flag for integrated instances.
	 *
	 * @param instance
	 *            the instance to update
	 * @param record
	 *            the record the source record to do additional checks if needed
	 * @throws EAIReportableException
	 *             on data process error
	 */
	protected void fillDefaultProperties(Instance instance, @SuppressWarnings("unused") CSItemRecord record)
			throws EAIReportableException {
		instance.add(EAIServicesConstants.PROPERTY_INTEGRATED_SYSTEM_ID, getName());
		instance.add(EAIServicesConstants.PROPERTY_INTEGRATED_FLAG_ID, Boolean.TRUE);
		// define the initial import type of the instance if not defined in the external mapping configuration
		if (!instance.isPropertyPresent(EAIServicesConstants.URI_EXTERNAL_TYPE, propertyNameResolver)) {
			instance.add(EAIServicesConstants.URI_EXTERNAL_TYPE, instance.getIdentifier(), propertyNameResolver);
		}
	}

	/**
	 * Fill received properties - record data converted using
	 * {@link EAIModelConverter#convertExternaltoSEIPProperties(Map, Instance)}.
	 *
	 * @param createdInstance
	 *            the created instance to update with converted properties
	 * @param record
	 *            the data to use as source
	 * @throws EAIException
	 *             on model convert error or any other sub error
	 */
	protected void fillReceivedProperties(Instance createdInstance, CSItemRecord record) throws EAIException {
		EAIModelConverter modelConverter = modelService.provideModelConverterByNamespace(record.getNamespace());
		Map<String, Serializable> convertExternaltoSEIPProperties = modelConverter
				.convertExternaltoSEIPProperties(record.getProperties(), createdInstance);
		createdInstance.getProperties().putAll(convertExternaltoSEIPProperties);

		// override the type of the instance. If the instance exists and it's type has been changed the above properties
		// update will override the changed type. This is not desired so we reset the type field back to the one known
		// in the system
		if (createdInstance.getIdentifier() != null) {
			createdInstance.add(DefaultProperties.TYPE, createdInstance.getIdentifier());
		}
	}

	/**
	 * Fill relations information in the instance. The relations should be added as 'references' property
	 *
	 * @param createdInstance
	 *            the created instance to update with converted properties
	 * @param record
	 *            the data to use as source
	 * @throws EAIReportableException
	 *             on model convert error
	 */
	protected void fillRelationsInformation(Instance createdInstance, CSItemRecord record)
			throws EAIReportableException {
		ModelConfiguration modelConfiguration = modelService.getModelConfigurationByNamespace(record.getNamespace());

		// collect the external references ids and join them as string
		// skip all but CMS - see ICD
		Map<CSExternalInstanceId, CSItemRelations> relationsByIds = mapRelationsByIds(record,
				e -> e.getRecord() != null && e.getRecord().getNamespace().equalsIgnoreCase(getDefaultNamespace()),
				modelConfiguration);
		if (relationsByIds.isEmpty()) {
			return;
		}
		// references is id by spec
		createdInstance.getProperties().put(EAIServicesConstants.PROPERTY_REFERENCES, String.join(", ",
				relationsByIds.keySet().stream().map(ExternalInstanceIdentifier::getExternalId).collect(Collectors.toList())));
	}

	/**
	 * Map relation wrapper by the external id associated with.
	 *
	 * @param data
	 *            the data to check relations for
	 * @param filter
	 *            the filter of {@link CSItemRelations} to skip in the result
	 * @param modelConfiguration
	 *            the model configuration to use
	 * @return the map if external id to {@link CSItemRelations} associated with
	 * @throws EAIReportableException
	 *             on any error to be reported
	 */
	protected Map<CSExternalInstanceId, CSItemRelations> mapRelationsByIds(CSItemRecord data,
			Predicate<CSItemRelations> filter, ModelConfiguration modelConfiguration) throws EAIReportableException {
		if (data.getRelationships() == null) {
			return Collections.emptyMap();
		}
		Iterator<CSItemRelations> relations = data.getRelationships().stream().filter(filter).iterator();
		Map<CSExternalInstanceId, CSItemRelations> relationsByIds = new HashMap<>(data.getRelationships().size());
		while (relations.hasNext()) {
			CSItemRelations csItemRelations = relations.next();
			CSExternalInstanceId externalId = extractExternalId(csItemRelations.getRecord(), modelConfiguration);
			relationsByIds.put(externalId, csItemRelations);
		}
		return relationsByIds;
	}

	/**
	 * Builds the namespace property id or return the the provided if already is in full uri format.
	 *
	 * @param namespace
	 *            the namespace
	 * @param localId
	 *            the local id
	 * @return the full uri
	 */
	protected static String buildNamespacePropertyId(String namespace, String localId) {
		if (localId == null) {
			return null;
		}
		if (localId.indexOf(':') > -1 || namespace == null) {
			return localId;
		}
		StringBuilder fullPropertyId = new StringBuilder(namespace.length() + localId.length() + 1);
		fullPropertyId.append(namespace);
		fullPropertyId.append(':');
		fullPropertyId.append(localId);
		return fullPropertyId.toString();
	}

	/**
	 * Gets the namespace of instance or generates an error if not found.
	 *
	 * @param item
	 *            the item to get for namespace
	 * @return the namespace
	 * @throws EAIReportableException
	 *             if namespace information is missing
	 */
	protected static String getNamespace(CSItemRecord item) throws EAIReportableException {
		if (StringUtils.isBlank(item.getNamespace())) {
			throw new EAIReportableException("Missing mandatory inforamtion 'namespace' for: " + item,
					String.valueOf(item));
		}
		return item.getNamespace();
	}

	/**
	 * Gets the default namespace.
	 *
	 * @return the default namespace
	 */
	protected abstract String getDefaultNamespace();

	/**
	 * Detect definition class based on {@link CSItemRecord} record.
	 *
	 * @param record
	 *            the record as source
	 * @return the class of definition for the provided record
	 * @throws EAIReportableException
	 *             on detection problem
	 */
	protected Class<? extends DefinitionModel> detectDefinitionClass(@SuppressWarnings("unused") CSItemRecord record)
			throws EAIReportableException {
		return GenericDefinition.class;
	}

	/**
	 * Detect definition id by checking the classification id or by using he default type provided.
	 *
	 * @param modelConfiguration
	 *            the model configuration
	 * @param item
	 *            the item record
	 * @param defaultType
	 *            the default type to use if could not be detected
	 * @return the definition id detected from metadata or the default
	 * @throws EAIException
	 *             on any error during detection or on missing data
	 */
	protected String detectDefinitionId(ModelConfiguration modelConfiguration, CSItemRecord item, String defaultType)
			throws EAIException {
		String classification = item.getClassification();
		if (StringUtils.isNotBlank(classification)) {
			EntityType typeByExternalName = modelConfiguration.getTypeByExternalName(classification);
			if (typeByExternalName == null) {
				throw new EAIReportableException(
						"Could not detect type of '" + classification + "' for "
								+ extractExternalId(item, modelConfiguration) + ". Check models!",
						String.valueOf(item));
			}
			return typeByExternalName.getIdentifier();
		}
		if (defaultType == null) {
			throw new EAIReportableException("Missing classification information for "
					+ extractExternalId(item, modelConfiguration) + ". Check response!", String.valueOf(item));
		}
		return defaultType;
	}

	/**
	 * Extract external type from the source record using the internal mappings if provided. First is checked if 'id'
	 * key exists which is considered as externalId and if so it is returned
	 *
	 * @param record
	 *            the item record to extract from
	 * @param modelConfiguration
	 *            the model configuration to use to search for external id
	 * @return the external id, null if not exist in the result, or throws {@link EAIRuntimeException} if mappings are
	 *         incomplete.
	 * @throws EAIReportableException
	 *             on inability to find the key or extract the value
	 */
	protected CSExternalInstanceId extractExternalId(CSItemRecord record, ModelConfiguration modelConfiguration)
			throws EAIReportableException {
		// the default key
		String idKey = buildNamespacePropertyId(getNamespace(record), EXTERNAL_KEY_ID);
		Object externalId = record.getProperties().get(idKey);
		if (externalId != null) {
			return new CSExternalInstanceId(String.valueOf(externalId),
					extractExternalSystemId(record, modelConfiguration));
		}
		LOGGER.warn("{} id not found as external id in {}", idKey, record.getProperties());
		if (modelConfiguration != null) {
			EntityType entityType = modelConfiguration.getTypeByExternalName(record.getClassification());
			if (entityType == null) {
				// probably wrong classification
				throw new EAIReportableException(
						"Failed to extract model type by classification: " + record.getClassification(),
						String.valueOf(record));
			}

			EntityProperty externalIdConfig = modelConfiguration.getPropertyByInternalName(entityType.getIdentifier(),
					EAIServicesConstants.URI_SUB_SYSTEM_ID);
			if (externalIdConfig == null) {
				// internal mapping issues
				throw new EAIReportableException(
						"Failed to find  external id mapping for type: " + entityType.getIdentifier(),
						String.valueOf(record));
			}
			Object value = record.getProperties().get(externalIdConfig.getDataMapping());
			if (value != null) {
				return new CSExternalInstanceId(String.valueOf(value),
						extractExternalSystemId(record, modelConfiguration));
			}
		}
		// error in model
		throw new EAIReportableException("Missing mandatory property: " + idKey, String.valueOf(record));
	}

	protected String extractExternalSystemId(CSItemRecord record, ModelConfiguration modelConfiguration)
			throws EAIReportableException {
		// the default key
		String idKey = buildNamespacePropertyId(getNamespace(record), EXTERNAL_KEY_SOURCE);
		Object sourceId = record.getProperties().get(idKey);
		if (sourceId != null) {
			return String.valueOf(sourceId);
		}
		LOGGER.warn("{} not found as external source system id in {}", idKey, record.getProperties());
		if (modelConfiguration != null) {
			EntityType entityType = modelConfiguration.getTypeByExternalName(record.getClassification());
			if (entityType == null) {
				// probably wrong classification
				throw new EAIReportableException(
						"Failed to extract model type by classification: " + record.getClassification(),
						String.valueOf(record));
			}

			EntityProperty externalIdConfig = modelConfiguration.getPropertyByInternalName(entityType.getIdentifier(),
					EAIServicesConstants.URI_SUB_SYSTEM_ID);
			if (externalIdConfig == null) {
				// internal mapping issues
				throw new EAIReportableException(
						"Failed to find external source system id mapping for type: " + entityType.getIdentifier(),
						String.valueOf(record));
			}
			Object value = record.getProperties().get(externalIdConfig.getDataMapping());
			if (value != null) {
				return String.valueOf(value);
			}
		}
		// error in model
		throw new EAIReportableException("Missing mandatory property: " + idKey, String.valueOf(record));
	}

	/**
	 * Get the external id key as uri. Example emf:externalID
	 *
	 * @param definitionId
	 *            the processed element definitionId
	 * @return the uri for the property as mapped in the model
	 */
	protected String getExternalIdUri(@SuppressWarnings("unused") String definitionId) {
		return EAIServicesConstants.URI_EXTERNAL_ID;
	}

	/**
	 * Normalize entry data by constructing full uri properties mapping. See
	 * {@link #normalizeEntryDataOnRecord(CSItemRecord)}
	 *
	 * @param items
	 *            the entries to normalize data for
	 * @throws EAIReportableException
	 *             on namespace resolving failure
	 */
	protected void normalizeEntryData(Collection<CSResultItem> items) throws EAIReportableException {
		for (CSResultItem csResultItem : items) {
			CSItemRecord metadata = csResultItem.getRecord();
			normalizeEntryDataOnRecord(metadata);
		}
	}

	/**
	 * Normalize {@link CSItemRecord} data by constructing full uri properties mapping. All original properties are
	 * removed and replaced with the normalized ones. Relations are iterated recursively
	 *
	 * @param record
	 *            is the record to normalize
	 * @throws EAIReportableException
	 *             on namespace resolving failure
	 */
	protected void normalizeEntryDataOnRecord(CSItemRecord record) throws EAIReportableException {

		String namespace = getNamespace(record);
		if (modelService.getModelConfigurationByNamespace(namespace) == null) {
			throw new EAIReportableException("Model contains data to unregistered system/namespace: " + namespace,
					String.valueOf(record));
		}
		Map<String, Object> normalizedProperties = new HashMap<>(record.getProperties().size());
		for (Entry<String, Object> entry : record.getProperties().entrySet()) {
			normalizedProperties.put(buildNamespacePropertyId(namespace, entry.getKey()), entry.getValue());
		}
		record.getProperties().clear();
		record.getProperties().putAll(normalizedProperties);
		if (record.getRelationships() != null) {
			for (CSItemRelations relations : record.getRelationships()) {
				normalizeEntryDataOnRecord(relations.getRecord());
			}
		}
	}

	/**
	 * Import base64 image data as an instance property.
	 *
	 * @param item
	 *            to use as source
	 * @param instance
	 *            the target instance to udate
	 */
	protected void loadThumbnailInInstance(CSResultItem item, Instance instance) {
		if (item.getThumbnail() == null) {
			return;
		}
		String base64Image = null;
		try {// might check for pattern like
				// "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			if (item.getThumbnail().startsWith("data:") || item.getThumbnail().length() > 1024) {
				base64Image = buildImageDataURIByString("image/jpeg", item.getThumbnail());
			} else {
				URI imageURI = URI.create(item.getThumbnail());
				if (!imageURI.isAbsolute()) {
					// client side
					base64Image = item.getThumbnail();
				} else {
					// server side download
					ResponseInfo response = communicationService.invoke(
							new RequestInfo(getName(), BaseEAIServices.DIRECT, new URIServiceRequest(imageURI)));
					base64Image = buildBase64Image(response);
				}
			}
		} catch (IllegalArgumentException e) {// NONSONAR
			// base64
			base64Image = buildImageDataURIByString("image/jpeg", item.getThumbnail());
		} catch (Exception e) {
			LOGGER.error("Failed to set thumbnail image for {} using: {} ", instance.getId(), item.getThumbnail(), e);
			return;
		}
		instance.add(DefaultProperties.THUMBNAIL_IMAGE, base64Image);
	}

	/**
	 * Builds and base64 image data. The wrapped response should be any {@link StreamingResponse}
	 *
	 * @param response
	 *            wrapped {@link StreamingResponse} as {@link ResponseInfo#getResponse()}
	 * @return the base64 image data in format <code>data:mimetype;base64,....</code>
	 * @throws EAIReportableException
	 *             on any error during communication
	 */
	protected static String buildBase64Image(ResponseInfo response) throws EAIReportableException {
		try (StreamingResponse streamer = (StreamingResponse) response.getResponse()) {
			byte[] bs = IOUtils.toByteArray(streamer.getStream());
			String encodeToString = Base64.getEncoder().encodeToString(bs);
			bs = null;
			return new StringBuilder(encodeToString.length() + 40)
					.append("data:")
						.append(streamer.getContentType())
						.append(";base64,")
						.append(encodeToString)
						.toString();
		} catch (Exception e) {
			throw new EAIReportableException("Failed to download and encode image ", e);
		}

	}

	private static String buildImageDataURIByString(String mimetype, String base64Image) {
		if (base64Image.startsWith("data:image/")) {
			return base64Image;
		}
		LOGGER.warn("Thumbnail encoded as base64 has no valid mimetype data: assume {}!", mimetype);
		return new StringBuilder("data:").append(mimetype).append(";base64,").append(base64Image).toString();
	}

	/**
	 * Store in cache an instance to be used at runtime.
	 *
	 * @param instance
	 *            the instance to store
	 */
	protected void storeInCache(Instance instance) {
		dbDao.saveOrUpdate(instance);
	}
}