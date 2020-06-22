package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.PROPERTY_INTEGRATED_FLAG_ID;
import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.PROPERTY_INTEGRATED_SYSTEM_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.PART_OF;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations.CREATE_OP;
import static com.sirmaenterprise.sep.eai.spreadsheet.service.IntegrationOperations.UPDATE_OP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.ProcessedInstanceModel;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.model.request.DynamicProperties;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.BeforeInstanceImportEvent;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response.RelationQueryProcessor.ContextProcessorParameters;

/**
 * Response reader and parser that transforms and maps the parsed raw data to internal SEP model. Supported raw model
 * for processing is {@link SpreadsheetSheet}. The returned data is not persisted set of instances as
 * {@link SpreadsheetResultInstances}. In addition if instance is already persisted it is loaded and updated with the
 * provided data.
 *
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIResponseReaderAdapter.PLUGIN_ID, order = 5)
public class SpreadsheetResponseReaderAdapter implements EAIResponseReaderAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelService modelService;
	@Inject
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Inject
	private InstanceService instanceService;
	@Inject
	private InstanceTypeResolver resolver;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private SearchService searchService;
	@Inject
	private RelationQueryProcessor contextQueryProcessor;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TaskExecutor taskExecutor;
	@Inject
	private ModelValidationService modelValidationService;
	@Inject
	private EventService eventService;

	@Override
	public String getName() {
		return SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends ProcessedInstanceModel> R parseResponse(ResponseInfo response) throws EAIException {
		if (response.getResponse() instanceof SpreadsheetSheet) {
			return (R) parseSheetModel(response);
		}
		throw new EAIException("Unsupported operation!");
	}

	private SpreadsheetResultInstances parseSheetModel(ResponseInfo response) {
		SpreadsheetSheet items = (SpreadsheetSheet) response.getResponse();
		List<SpreadsheetEntry> entries = items.getEntries();
		SpreadsheetResultInstances parsedInstances = new SpreadsheetResultInstances();
		if (entries.isEmpty()) {
			parsedInstances
					.setError(new EAIReportableException("There is no data to process. Please check your file!"));
			return parsedInstances;
		}

		TimeTracker createAndStart = TimeTracker.createAndStart();
		parsedInstances.setInstances(Collections.synchronizedList(new ArrayList<>(entries.size())));
		Map<String, SpreadsheetEntry> dbIdsMap = getDbIdsFromSpreadSheet(entries);
		Map<SpreadsheetEntryId, InstanceReference> existing = loadExistingItems(dbIdsMap);
		SpreadsheetReadServiceRequest request = (SpreadsheetReadServiceRequest) response.getRequest();
		Instance context = request.getContext() != null ? request.getContext().toInstance() : null;
		ModelConfiguration modelConfiguration = getModelConfiguration();
		int fragments = FragmentedWork.computeBatchForNFragments(entries.size(),
				eaiConfiguration.getParallelismCount().get());
		EntryBasedValidationReport errorBuilder = new EntryBasedValidationReport();
		Collection<Future<?>> futures = FragmentedWork.doWorkAndReduce(entries, fragments, data -> taskExecutor.submit(
				() -> processBatchEntries(data, modelConfiguration, context, existing, parsedInstances, errorBuilder)));
		taskExecutor.waitForAll(futures);
		if (errorBuilder.hasErrors()) {
			parsedInstances.setError(new EAIReportableException(errorBuilder.build(), "Check the imported file."));
			LOGGER.error("Error during spreadsheet processing!", parsedInstances.getError());
		}

		LOGGER.debug("Search and processing in {} took {} ms and returned {} instances", getName(),
				createAndStart.stop(), parsedInstances.getInstances().size());
		return parsedInstances;
	}

	private void processBatchEntries(Collection<SpreadsheetEntry> entries, ModelConfiguration modelConfiguration,
			Instance context, Map<SpreadsheetEntryId, InstanceReference> existing,
			SpreadsheetResultInstances parsedInstances, EntryBasedValidationReport errorBuilder) {
		for (SpreadsheetEntry entry : entries) {
			SpreadsheetEntryId externalId = null;
			try {
				externalId = extractExternalId(entry);
				LOGGER.debug("Processing entry {} with id {}", entry, externalId);
				String definitionId = detectDefinitionId(entry, modelConfiguration);
				Instance specifiedContext = detectSpecifiedContext(context, entry, definitionId);
				InstanceReference instanceReference = existing.get(externalId);
				Instance instance;
				if (instanceReference == null) {
					instance = createImportableInstance(specifiedContext, definitionId);
				} else {
					instance = instanceReference.toInstance();
				}
				// if instance is loaded or currently created
				if (instance != null) {
					IntegrationData integrationData = new IntegrationData(instance, specifiedContext, entry, externalId,
							(instanceReference == null ? CREATE_OP : UPDATE_OP).getOperation(), modelConfiguration);
					validateAndFillReceivedData(integrationData);
					appendResult(parsedInstances, integrationData);
				}
			} catch (Exception e) {// NOSONAR
				errorLogging(errorBuilder, entry, externalId, e.getMessage());
			}
		}
	}

	private void appendResult(SpreadsheetResultInstances parsedInstances, IntegrationData integrationData) {
		String dbId = getDbIdProperty(integrationData.getSource());
		if (dbId == null) {
			LOGGER.warn("Could not detect property for db id. Check models!");
		} else {
			integrationData.getSource().getProperties().put(dbId, integrationData.getIntegrated().getId());
		}
		parsedInstances.getInstances().add(new ParsedInstance(integrationData.getId(), integrationData.getIntegrated(),
				integrationData.getSource(), integrationData.getContext(), integrationData.getOperation()));
	}

	private Map<SpreadsheetEntryId, InstanceReference> loadExistingItems(Map<String, SpreadsheetEntry> dbIdsMap) {
		return resolver.resolveReferences(dbIdsMap.keySet()).stream().collect(
				Collectors.toMap(ref -> extractExternalId(dbIdsMap.get(ref.getId())), Function.identity()));
	}

	private Map<String, SpreadsheetEntry> getDbIdsFromSpreadSheet(List<SpreadsheetEntry> spreadsheetEntries) {
		Map<String, SpreadsheetEntry> internalToExternalIdMapper = new HashMap<>(spreadsheetEntries.size());
		for (SpreadsheetEntry entry : spreadsheetEntries) {
			String dbId = getDbId(entry);
			if (StringUtils.isNotBlank(dbId)) {
				internalToExternalIdMapper.put(dbId, entry);
			}
		}

		return internalToExternalIdMapper;
	}

	private String getDbId(SpreadsheetEntry entry) {
		String dbIdProperty = getDbIdProperty(entry);
		if (dbIdProperty != null) {
			return Objects.toString(entry.getProperties().get(dbIdProperty), null);
		}
		return null;
	}

	private String getDbIdProperty(SpreadsheetEntry entry) {
		String defaultIdURI = eaiConfiguration.getIdentifierPropertyURI().get();
		if (StringUtils.isNotBlank(defaultIdURI)) {
			return defaultIdURI;
		}
		ModelConfiguration modelConfiguration = getModelConfiguration();
		EntityType entityType = modelConfiguration.getTypeByExternalName(getTypePropertyValue(entry));
		// on this stage of processing ignore model errors
		if (entityType == null) {
			return null;
		}
		EntityProperty idProperty = modelConfiguration.getPropertyByFilter(entityType.getIdentifier(),
				property -> DefaultProperties.UNIQUE_IDENTIFIER.equals(property.getPropertyId()));
		if (idProperty == null) {
			return null;
		}
		return idProperty.getUri();
	}

	private static void errorLogging(EntryBasedValidationReport errorBuilder, SpreadsheetEntry record,
			SpreadsheetEntryId externalId, String cause) {
		Object recordId = externalId != null ? externalId : record;
		LOGGER.debug("Error on {}={} with details '{}'. Record '{}'", recordId, cause, record, cause);
		errorBuilder.setAndAppend(externalId, cause);
	}

	private Instance createImportableInstance(Instance context, String definitionId) throws EAIException {
		DefinitionModel definitionModel = definitionService.find(definitionId);
		if (definitionModel == null) {
			throw new EAIException("Failed to load definition model for: " + definitionId);
		}
		Instance instance = instanceService.createInstance(definitionModel, context, CREATE_OP.getOperation());
		fillSystemProperties(instance);
		return instance;
	}

	private Instance detectSpecifiedContext(Instance defaultContext, SpreadsheetEntry source, String definitionId)
			throws EAIException {
		DynamicProperties sourceData = source.getProperties();
		if (sourceData.containsKey(PART_OF)) {
			String configuration = source.getBinding(PART_OF);
			EAIModelConverter modelConverter = modelService.provideModelConverter(getName());
			final Pair<String, Serializable> converted = modelConverter.convertExternaltoSEIPProperty(PART_OF,
					(Serializable) sourceData.get(PART_OF), definitionId);
			String partOfValue = extractSingleStringValue(converted.getSecond());
			if (StringUtils.isNotBlank(configuration)) {
				// single string id should result in single instance
				// should load the instance data as the instance later is used for permission calculation and if not
				// loaded
				// causes : CMF-25692
				Instance found = findAndLoadRelatedAndByConfiguration(defaultContext, PART_OF, configuration,
						partOfValue);
				// remove configuration to optimize double processing
				source.unbind(PART_OF);
				// and now remove the value that should not be processed - this property should be handled automatically
				// in backend so no need to add it to the new instance
				sourceData.remove(PART_OF);
				return found;
			}
			return resolver
					.resolveReference(partOfValue)
						.orElseThrow(() -> new EAIException(
								"Object context could not be set! Missing instance with id: " + partOfValue))
						.toInstance();
		}
		// just return the default context
		return defaultContext;

	}

	private static String extractSingleStringValue(Serializable value) throws EAIReportableException {
		Object valueLocal = value;
		if (valueLocal instanceof Collection && ((Collection<?>) valueLocal).size() == 1) {
			valueLocal = ((Collection<?>) valueLocal).iterator().next();
		}
		if (valueLocal instanceof String) {
			return valueLocal.toString();
		}
		throw new EAIReportableException(
				"Object context could not be set! Unexpected " + PART_OF + " relation value: " + value);
	}

	/**
	 * Fill default properties - integrated system id and flag for integrated instances.
	 *
	 * @param instance the instance to update
	 */
	private void fillSystemProperties(Instance instance) {
		instance.add(PROPERTY_INTEGRATED_SYSTEM_ID, getName());
		instance.add(PROPERTY_INTEGRATED_FLAG_ID, Boolean.TRUE);
	}

	/**
	 * Fill received properties - record data converted using
	 * {@link EAIModelConverter#convertExternaltoSEIPProperties(Map, Instance)}. Prior of converting are executed
	 * several validation and if any single error is detected an {@link EAIReportableException} is thrown with the
	 * details.
	 *
	 * @param integrated the created instance to update with converted properties
	 * @throws EAIReportableException on model convert error or any other validation error
	 */
	private void validateAndFillReceivedData(IntegrationData integrated) throws EAIReportableException {
		Instance context = integrated.getContext();

		SpreadsheetEntry record = integrated.getSource();
		Instance instance = integrated.getIntegrated();
		LOGGER.trace("Validating and processing instance {} with context {} and source data {} ", instance,
				context == null ? "null" : context.getId(), record);
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		modelValidationService.validatePropertyModel(integrated, errorBuilder);
		Map<String, Serializable> convertExternaltoSEIPProperties = null;
		try {
			EAIModelConverter modelConverter = modelService.provideModelConverter(getName());
			convertExternaltoSEIPProperties = modelConverter.convertExternaltoSEIPProperties(record.getProperties(),
					instance);
		} catch (Exception e) {// NOSONAR
			errorBuilder.separator().append(e.getMessage());
		}
		// now process object properties and collect errors
		try {
			setRelations(integrated, convertExternaltoSEIPProperties);
			if (convertExternaltoSEIPProperties != null) {
				instance.addAllProperties(convertExternaltoSEIPProperties);
			}
		} catch (Exception e) {// NOSONAR
			errorBuilder.separator().append(e.getMessage());
		}

		eventService.fire(new BeforeInstanceImportEvent(integrated.getIntegrated()));

		modelValidationService.validateInstanceModel(integrated, errorBuilder);
		modelValidationService.validateExistingInContext(integrated, errorBuilder);
		if (errorBuilder.hasErrors()) {
			throw new EAIReportableException(errorBuilder.build());
		}
	}

	private void setRelations(IntegrationData integrated, Map<String, Serializable> converted) throws EAIException {
		SpreadsheetEntry record = integrated.getSource();
		Instance instance = integrated.getIntegrated();
		if (MapUtils.isEmpty(record.getBindings()) || converted == null) {
			return;
		}
		String identifier = instance.getIdentifier();
		// execute in parallel the searches
		Stream<PropertyDefinition> objectProperties = definitionService
				.find(identifier)
					.fieldsStream()
					// if object property
					.filter(PropertyDefinition.isObjectProperty())
					// if search query is provided (otherwise data will be set as is) && if there is any value to set
					.filter(definition -> getSearchConfigurationByDefinition(integrated.getSource(), definition) != null
							&& converted.get(getFieldIdByDefinition(definition)) != null);
		Spliterator<PropertyDefinition> spliterator = objectProperties.spliterator();
		Stream<PropertyDefinition> stream = StreamSupport.stream(spliterator, spliterator.estimateSize() > 5);
		final ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		Consumer<PropertyDefinition> security = securityContextManager
				.executeAsSystem()
					.toWrapper()
					.consumer(propertyDefinition -> {
					try {
						synchronized (instance) {
							setRelationsByPropertyDefinition(integrated,
									converted.remove(getFieldIdByDefinition(propertyDefinition)),
									getSearchConfigurationByDefinition(integrated.getSource(), propertyDefinition),
									propertyDefinition);
						}
					} catch (Exception e) {// NOSONAR
						errorBuilder.separator().append("Error on object property: " + propertyDefinition.getUri()
								+ " with details: " + e.getMessage());
					}
				});
		stream.forEach(security);

		if (errorBuilder.hasErrors()) {
			throw new EAIReportableException(errorBuilder.build());
		}
	}

	private void setRelationsByPropertyDefinition(IntegrationData integrated, Serializable value, String configuration,
			PropertyDefinition propertyDefinition) throws EAIException {
		if (configuration == null) {
			mergeData(integrated, propertyDefinition, value);
			return;
		}
		mergeData(integrated, propertyDefinition, findRelationsByConfiguration(integrated.getContext(), value,
				configuration, getFieldIdByDefinition(propertyDefinition)));
	}

	@SuppressWarnings("unchecked")
	private Serializable findRelationsByConfiguration(Instance searchContext, Serializable value, String configuration,
			String fieldIdByDefinition) throws EAIException {
		if (value instanceof Collection) {
			ArrayList<Serializable> localValue = new ArrayList<>(((Collection<Serializable>) value).size());
			for (Serializable element : (Collection<Serializable>) value) {
				localValue.add(findRelationByConfiguration(searchContext, fieldIdByDefinition, configuration, element)
						.getId());
			}
			return localValue;
		}
		return findRelationByConfiguration(searchContext, fieldIdByDefinition, configuration, value).getId();
	}

	private Instance findRelationByConfiguration(Instance searchContext, String fieldId, String configuration,
			Serializable value) throws EAIException {
		int limit = (value instanceof Collection ? ((Collection<?>) value).size() : 1) + 1;
		// limit to reduced size just to indicate possible error without further details
		List<Instance> references = searchRelation(configuration, value, searchContext, limit, false);
		return getValidResult(fieldId, configuration, references);
	}

	private Instance findAndLoadRelatedAndByConfiguration(Instance searchContext, String fieldId, String configuration,
			Serializable value) throws EAIException {
		int limit = (value instanceof Collection ? ((Collection<?>) value).size() : 1) + 1;
		// limit to reduced size just to indicate possible error without further details
		List<Instance> references = searchRelation(configuration, value, searchContext, limit, true);
		return getValidResult(fieldId, configuration, references);
	}

	private static Instance getValidResult(String fieldId, String configuration, List<Instance> references)
			throws EAIReportableException {
		if (references.isEmpty()) {
			throw new EAIReportableException("Object/s for field " + fieldId
					+ " could not be found in the system! Check query: " + configuration);
		} else if (references.size() > 1) {
			throw new EAIReportableException(
					"More than one objects are found for field: " + fieldId + "! Check query: " + configuration);
		}
		return references.get(0);
	}

	private static void mergeData(IntegrationData data, PropertyDefinition propertyDefinition, Serializable value)
			throws EAIReportableException {
		mergeData(data.getIntegrated(), getFieldIdByDefinition(propertyDefinition),
				propertyDefinition.isMultiValued(), value);
	}

	@SuppressWarnings("unchecked")
	private static void mergeData(Instance instance, String fieldId, boolean multivalue, Serializable newValue)
			throws EAIReportableException {
		if (multivalue) {
			// override multi value field
			if (newValue instanceof Collection) {
				instance.appendAll(fieldId, (Collection<? extends Serializable>) newValue);
			} else
				instance.append(fieldId, newValue);
		} else {
			if (newValue instanceof Collection) {
				throw new EAIReportableException("Provided multivalue for single property: " + fieldId);
			}
			// override single value field
			instance.add(fieldId, newValue);
		}
	}

	private List<Instance> searchRelation(String fieldConfiguration, Serializable value, Instance context, int limit,
			boolean loadData) throws EAIException {
		ContextProcessorParameters params = RelationQueryProcessor.prepareParameters(fieldConfiguration);
		params.setContext(context).setProvidedValue(value);
		Condition query = contextQueryProcessor.convertToCondtion(params);
		SearchRequest request = new SearchRequest(new HashMap<>());
		request.setDialect(SearchDialects.SPARQL);
		request.setSearchTree(query);
		SearchArguments<Instance> relationSearch = searchService.parseRequest(request);
		// limit search to improve performance
		relationSearch.setPageSize(limit);
		relationSearch.setMaxSize(relationSearch.getPageSize());
		if (loadData) {
			searchService.searchAndLoad(Instance.class, relationSearch);
		} else {
			searchService.search(Instance.class, relationSearch);
		}
		return relationSearch.getResult();
	}

	private String detectDefinitionId(SpreadsheetEntry item, ModelConfiguration modelConfiguration)
			throws EAIException {
		String definitionName = getTypePropertyValue(item);
		if (StringUtils.isNotBlank(definitionName)) {
			EntityType entityType = loadEntityType(definitionName, modelConfiguration, item);
			modelValidationService.validateCreatablePermissions(entityType);
			return entityType.getIdentifier();
		}
		throw new EAIReportableException("Missing mandatory field for type (" + getTypePropertyURI() + ")");
	}

	private static EntityType loadEntityType(final String definitionName, ModelConfiguration modelConfiguration,
			SpreadsheetEntry item) throws EAIReportableException {
		EntityType entityType = modelConfiguration.getTypeByExternalName(definitionName);
		if (entityType == null) {
			throw new EAIReportableException("There is no type " + definitionName + " in the system!",
					String.valueOf(item));
		}
		return entityType;
	}

	/**
	 * Extract external type from the source record provided.
	 *
	 * @param record the item record to extract from
	 * @return the external id, or throws {@link EAIReportableException} if source record is incomplete
	 * @throws EAIRuntimeException on inability to find the key or extract the value
	 */
	private static SpreadsheetEntryId extractExternalId(SpreadsheetEntry record) {
		// the default key
		String externalId = record.getExternalId();
		if (externalId != null && record.getSheet() != null) {
			return new SpreadsheetEntryId(record.getSheet(), externalId);
		}
		// error in model
		throw new EAIRuntimeException("Missing mandatory property: row id or row sheet!");
	}

	private static String getFieldIdByDefinition(PropertyDefinition propertyDefinition) {
		return propertyDefinition.getIdentifier();
	}

	private static String getSearchConfigurationByDefinition(SpreadsheetEntry entry, PropertyDefinition definition) {
		return entry.getBinding(definition.getUri());
	}

	private String getTypePropertyValue(SpreadsheetEntry item) {
		return Objects.toString(item.getProperties().get(getTypePropertyURI()), null);
	}

	private String getTypePropertyURI() {
		return eaiConfiguration.getTypePropertyURI().get();
	}

	private ModelConfiguration getModelConfiguration() {
		return modelService.getModelConfiguration(getName());
	}
}
