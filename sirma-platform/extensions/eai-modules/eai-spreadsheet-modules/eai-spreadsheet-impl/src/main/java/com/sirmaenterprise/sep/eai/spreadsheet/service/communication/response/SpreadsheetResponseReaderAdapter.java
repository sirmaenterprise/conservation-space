package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.NEW_LINE;
import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.PROPERTY_INTEGRATED_FLAG_ID;
import static com.sirma.itt.seip.eai.service.model.EAIBaseConstants.PROPERTY_INTEGRATED_SYSTEM_ID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.ProcessedInstanceModel;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.SemanticInstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport;
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
	/** prepare import. */
	private static final Operation CREATE = new Operation(ActionTypeConstants.CREATE);

	@Inject
	private ModelService modelService;
	@Inject
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Inject
	private InstanceService instanceService;
	@Inject
	private InstanceTypeResolver resolver;
	@Inject
	private DictionaryService dictionaryService;
	@Inject
	private Validator validator;
	@Inject
	private SemanticInstanceTypes semanticInstanceTypes;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private InstanceAccessEvaluator instanceAccessEvaluator;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private SearchService searchService;
	@Inject
	private RelationQueryProcessor contextQueryProcessor;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TaskExecutor taskExecutor;

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
		SpreadsheetValidationReport errorBuilder = new SpreadsheetValidationReport();
		parsedInstances.setInstances(new ArrayList<>(entries.size()));
		SpreadsheetReadServiceRequest request = (SpreadsheetReadServiceRequest) response.getRequest();
		Map<SpreadsheetEntryId, InstanceReference> existing = loadExistingItems(entries);
		Instance context = request.getContext() != null ? request.getContext().toInstance() : null;
		ModelConfiguration modelConfiguration = getModelConfiguration();
		int fragments = FragmentedWork.computeBatchForNFragments(entries.size(),
				eaiConfiguration.getParallelismCount().get().intValue());
		Collection<Future<?>> futures = FragmentedWork.doWorkAndReduce(entries, fragments, data -> taskExecutor.submit(
				() -> processBatchEntries(data, modelConfiguration, context, existing, parsedInstances, errorBuilder)));
		taskExecutor.waitForAll(futures);
		if (errorBuilder.hasErrors()) {
			parsedInstances.setError(new EAIReportableException(errorBuilder.get().toString(),
					Objects.toString(items.getEntries(), "")));
			LOGGER.error("Error during spreadsheet processing!", parsedInstances.getError());
		}

		LOGGER.debug("Search and processing in {} took {} ms and returned {} instances",

				getName(), Long.valueOf(createAndStart.stop()), Integer.valueOf(parsedInstances.getInstances().size()));
		return parsedInstances;
	}

	private void processBatchEntries(Collection<SpreadsheetEntry> entries, ModelConfiguration modelConfiguration,
			Instance context, Map<SpreadsheetEntryId, InstanceReference> existing,
			SpreadsheetResultInstances parsedInstances, SpreadsheetValidationReport errorBuilder) {
		for (SpreadsheetEntry entry : entries) {
			SpreadsheetEntryId externalId = null;
			try {
				externalId = extractExternalId(entry);
				LOGGER.debug("Processing entry {} with id {}", entry, externalId);
				String definitionId = detectDefinitionId(entry, modelConfiguration);
				InstanceReference instanceReference = existing.get(externalId);
				Instance instance;
				if (instanceReference == null) {
					instance = createImportableInstance(context, definitionId);
				} else {
					instance = instanceReference.toInstance();
				}
				// if instance is loaded or currently created
				if (instance != null) {
					validateAndFillReceivedData(instance, context, entry);
					appendResult(parsedInstances, externalId, entry, instance, context);
				}
			} catch (Exception e) {// NOSONAR
				errorLogging(errorBuilder, entry, externalId, e.getMessage());
			}
		}
	}

	private void appendResult(SpreadsheetResultInstances parsedInstances, SpreadsheetEntryId externalId,
			SpreadsheetEntry source, Instance instance, Instance context) throws EAIReportableException {
		String dbId = getDbIdProperty(source);
		if (dbId == null) {
			LOGGER.warn("Could not detect property for db id. Check models!");
		} else {
			source.getProperties().put(dbId, instance.getId());
		}
		parsedInstances.getInstances().add(new ParsedInstance(externalId, instance, source, context));
	}

	private Map<SpreadsheetEntryId, InstanceReference> loadExistingItems(List<SpreadsheetEntry> normalized) {
		Map<String, SpreadsheetEntry> internalToExternalIdMapper = new HashMap<>(normalized.size());
		for (SpreadsheetEntry entry : normalized) {
			String dbId = getDbId(entry);
			if (StringUtils.isNotBlank(dbId)) {
				internalToExternalIdMapper.put(dbId, entry);
			}
		}
		return resolver.resolveReferences(internalToExternalIdMapper.keySet()).stream().collect(Collectors.toMap(
				ref -> extractExternalId(internalToExternalIdMapper.get(ref.getIdentifier())), Function.identity()));
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

	private static void errorLogging(ErrorBuilderProvider errorBuilder, SpreadsheetEntry record,
			SpreadsheetEntryId externalId, String cause) {
		Object recordId = externalId != null ? externalId : record;
		LOGGER.debug("Error on {}={} with details '{}'. Record '{}'", recordId, cause, record, cause);
		errorBuilder.get(1024).append("Row=").append(recordId).append(": ").append(cause).append(NEW_LINE);
	}

	private Instance createImportableInstance(Instance context, String definitionId) throws EAIException {
		DefinitionModel definitionModel = dictionaryService.find(definitionId);
		if (definitionModel == null) {
			throw new EAIException("Failed to load definition model for: " + definitionId);
		}
		Instance instance = instanceService.createInstance(definitionModel, context, CREATE);
		fillDefaultProperties(instance);
		return instance;
	}

	/**
	 * Fill default properties - integrated system id and flag for integrated instances.
	 *
	 * @param instance
	 *            the instance to update
	 * @throws EAIReportableException
	 *             on data process error
	 */
	private void fillDefaultProperties(Instance instance) throws EAIReportableException {
		instance.add(PROPERTY_INTEGRATED_SYSTEM_ID, getName());
		instance.add(PROPERTY_INTEGRATED_FLAG_ID, Boolean.TRUE);
	}

	/**
	 * Fill received properties - record data converted using
	 * {@link EAIModelConverter#convertExternaltoSEIPProperties(Map, Instance)}. Prior of converting are executed
	 * several validation and if any single error is detected an {@link EAIReportableException} is thrown with the
	 * details.
	 *
	 * @param createdInstance
	 *            the created instance to update with converted properties
	 * @param context
	 *            is the current context to work under
	 * @param record
	 *            the data to use as source
	 * @throws EAIReportableException
	 *             on model convert error or any other validation error
	 */
	private void validateAndFillReceivedData(Instance createdInstance, Instance context, SpreadsheetEntry record)
			throws EAIReportableException {

		EntityType entityType = getModelConfiguration().getTypeByDefinitionId(createdInstance.getIdentifier());
		ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();

		try {
			validatePropertyModel(record, entityType);
		} catch (Exception e) {// NOSONAR
			appendLog(errorBuilder, e.getMessage());
		}
		Map<String, Serializable> convertExternaltoSEIPProperties = null;
		try {
			EAIModelConverter modelConverter = modelService.provideModelConverter(getName());
			convertExternaltoSEIPProperties = modelConverter.convertExternaltoSEIPProperties(record.getProperties(),
					createdInstance);
		} catch (Exception e) {// NOSONAR
			appendLog(errorBuilder, e.getMessage());
		}
		// now process object properties and collect errors
		try {
			setRelations(createdInstance, context, convertExternaltoSEIPProperties, record);
			createdInstance.getProperties().putAll(convertExternaltoSEIPProperties);
		} catch (Exception e) {// NOSONAR
			appendLog(errorBuilder, e.getMessage());
		}

		ValidationContext validationContext = new ValidationContext(createdInstance, CREATE);
		validator.validate(validationContext);
		if (!validationContext.getMessages().isEmpty()) {
			appendLog(errorBuilder, "Data validation errors: " + validationContext.getMessages());
		}
		if (errorBuilder.hasErrors()) {
			throw new EAIReportableException(errorBuilder.get().toString());
		}
	}

	private void setRelations(Instance createdInstance, Instance context, Map<String, Serializable> converted,
			SpreadsheetEntry record) throws EAIException {
		Map<String, String> configurations = record.getConfiguration();
		if (configurations == null || configurations.isEmpty()) {
			return;
		}
		String identifier = createdInstance.getIdentifier();
		// execute in parallel the searches

		Stream<PropertyDefinition> objectProperties = dictionaryService
				.find(identifier)
					.fieldsStream()
					// if object property
					.filter(PropertyDefinition.isObjectProperty())
					// if search query is provided (otherwise data will be set as is) && if there is any value to set
					.filter(definition -> configurations.get(definition.getUri()) != null
							&& converted.get(definition.getIdentifier()) != null);
		Spliterator<PropertyDefinition> spliterator = objectProperties.spliterator();
		Stream<PropertyDefinition> stream = StreamSupport.stream(spliterator, spliterator.estimateSize() > 5);
		final ErrorBuilderProvider errorBuilder = new ErrorBuilderProvider();
		Consumer<PropertyDefinition> security = securityContextManager
				.executeAsSystem()
					.toWrapper()
					.consumer(propertyDefinition -> {
						try {
							setRelationsByPropertyDefinition(createdInstance,
									converted.remove(propertyDefinition.getIdentifier()), context,
									configurations.get(propertyDefinition.getUri()), propertyDefinition);
						} catch (Exception e) {// NOSONAR
							appendLog(errorBuilder, "Error on object property: " + propertyDefinition.getUri()
									+ " with details: " + e.getMessage());
						}
					});
		stream.forEach(security);

		if (errorBuilder.hasErrors()) {
			throw new EAIReportableException(errorBuilder.toString());
		}
	}

	@SuppressWarnings("unchecked")
	private void setRelationsByPropertyDefinition(Instance createdInstance, Serializable value, Instance context,
			String configuration, PropertyDefinition propertyDefinition) throws EAIException {
		String fieldId = propertyDefinition.getIdentifier();
		//
		if (configuration == null) {
			mergeData(createdInstance, propertyDefinition, value);
			return;
		}
		if (value instanceof Collection) {
			for (Serializable element : (Collection<Serializable>) value) {
				setRelationsByPropertyDefinition(createdInstance, element, context, configuration, propertyDefinition,
						fieldId);
			}
		} else {
			setRelationsByPropertyDefinition(createdInstance, value, context, configuration, propertyDefinition,
					fieldId);
		}

	}

	private void setRelationsByPropertyDefinition(Instance createdInstance, Serializable value, Instance context,
			String configuration, PropertyDefinition propertyDefinition, String fieldId) throws EAIException {
		int limit = (value instanceof Collection ? ((Collection<?>) value).size() : 1) + 1;
		// limit to reduced size just to indicate possible error without further details
		List<Instance> references = searchRelation(configuration, value, context, limit);
		if (references.isEmpty()) {
			throw new EAIReportableException("Object/s for field " + fieldId
					+ " could not be found in the system! Check query: " + configuration);
		} else if (references.size() > 1) {
			throw new EAIReportableException(
					"More than one objects are found for field: " + fieldId + "! Check query: " + configuration);
		}
		mergeData(createdInstance, propertyDefinition, references.get(0).getId());
	}

	@SuppressWarnings("unchecked")
	private static void mergeData(Instance createdInstance, PropertyDefinition propertyDefinition, Serializable value)
			throws EAIReportableException {
		String fieldId = propertyDefinition.getIdentifier();
		Serializable existing = createdInstance.get(fieldId);

		if (propertyDefinition.isMultiValued().booleanValue()) {
			Collection<Serializable> updated;
			if (existing instanceof Collection) {
				updated = CollectionUtils.addValue(new LinkedHashSet<>((Collection<Serializable>) existing), value,
						true);
			} else {
				if (existing != null) {
					updated = CollectionUtils.addValue(new LinkedHashSet<>(new ArrayList<>()), existing, true);
					updated = CollectionUtils.addValue(new LinkedHashSet<>(updated), value, true);
				} else {
					updated = CollectionUtils.addValue(new LinkedHashSet<>(new ArrayList<>()), value, true);
				}
			}
			createdInstance.add(fieldId, new ArrayList<>(updated));
		} else {
			if (value instanceof Collection) {
				throw new EAIReportableException("Provided multivalue for single property: " + fieldId);
			}
			// override
			createdInstance.add(fieldId, value);
		}
	}

	private List<Instance> searchRelation(String fieldConfiguration, Serializable value, Instance context, int limit)
			throws EAIException {
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
		// search and don't load instances since not needed
		searchService.search(Instance.class, relationSearch);
		return relationSearch.getResult();
	}

	private SpreadsheetResponseReaderAdapter appendLog(ErrorBuilderProvider errorBuilder, String message) {
		if (errorBuilder.hasErrors()) {
			errorBuilder.append(NEW_LINE);
		}
		errorBuilder.append(message);
		return this;
	}

	private String detectDefinitionId(SpreadsheetEntry item, ModelConfiguration modelConfiguration)
			throws EAIException {
		String definitionName = getTypePropertyValue(item);
		if (StringUtils.isNotBlank(definitionName)) {
			EntityType entityType = loadEntityType(definitionName, modelConfiguration, item);
			InstanceType instanceType = semanticInstanceTypes.from(entityType.getUri()).orElseThrow(// NOSONAR
					() -> new EAIRuntimeException("Missing expexted class definition for " + definitionName));
			if (!instanceType.isCreatable()) {
				if (instanceType.isUploadable()) {
					throw new EAIReportableException(
							"Object of type " + definitionName + " is not allowed to be created!");
				}
				throw new EAIReportableException(
						"Object of type " + definitionName + " is neither allowed to be created or uploaded!");
			}
			validateCreatablePermissions(entityType);
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

	private void validatePropertyModel(SpreadsheetEntry entry, EntityType typeByExternalName)
			throws EAIReportableException {
		// remove the non model system properties
		removeSystemProperties(entry);
		// validate if property is not part of the model
		String definitionId = typeByExternalName.getIdentifier();
		EntityType type = getModelConfiguration().getTypeByDefinitionId(definitionId);
		Function<EntityProperty, String> propertyMapper = EntityProperty::getUri;
		Set<String> modelProperties = type.getProperties().stream().map(propertyMapper).collect(Collectors.toSet());
		Predicate<String> nonModelFilter = propertyId -> !modelProperties.contains(propertyId);
		Set<String> nonModelProperties = entry
				.getProperties()
					.keySet()
					.stream()
					.filter(nonModelFilter)
					.collect(Collectors.toSet());
		if (!nonModelProperties.isEmpty()) {
			int errorsCount = nonModelProperties.size();
			StringBuilder errorMessage = new StringBuilder();
			errorMessage
					.append("Propert")
						.append(errorsCount > 1 ? "ies " : "y ")
						.append(nonModelProperties)
						.append(errorsCount > 1 ? " are " : " is ")
						.append("not valid for ")
						.append(type.getTitle());
			throw new EAIReportableException(errorMessage.toString());
		}
	}

	private static void removeSystemProperties(SpreadsheetEntry entry) {
		entry.getProperties().remove(EAISpreadsheetConstants.IMPORT_STATUS);
	}

	private void validateCreatablePermissions(EntityType typeByExternalName) throws EAIException {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(typeByExternalName.getUri());
		if (classInstance == null) {
			throw new EAIException("Class instance '" + typeByExternalName.getUri() + "' could not be resolved");
		}
		Serializable uri = classInstance.getId();
		if (!instanceAccessEvaluator
				.canWrite(Objects.toString(typeConverter.convert(ShortUri.class, uri), uri.toString()))) {
			throw new EAIReportableException("The current user doesn't have permissions to create object of type: "
					+ typeByExternalName.getTitle());
		}
	}

	/**
	 * Extract external type from the source record provided.
	 *
	 * @param record
	 *            the item record to extract from
	 * @return the external id, or throws {@link EAIReportableException} if source record is incomplete
	 * @throws EAIRuntimeException
	 *             on inability to find the key or extract the value
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