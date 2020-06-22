package com.sirma.itt.sep.instance.unique;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.sep.instance.unique.loader.UniqueValueLoader;
import com.sirma.itt.sep.instance.unique.persistence.UniqueField;
import com.sirma.itt.sep.instance.unique.persistence.UniqueValueDao;

/**
 * A implementation of {@link UniqueValueValidationService}.
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
public class UniqueValueValidationServiceImpl implements UniqueValueValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int BATCH_SIZE = 100;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Finds all the unique values for the couple "definitionId" and "fieldUri"
	 * and returns the oldest instance for everyone of them. This query have to be used for values different than date.<br>
	 * Query is :
	 * <pre><b>
	 *     select ?instance ?uniqueValue where {
	 *         ?instance emf:definitionId ?definitionIdVariable.
	 *         ?instance emf:isDeleted "false"^^xsd:boolean.
	 *         ?instance emf:createdOn ?createdOn.
	 *         ?instance ?fieldUriVariable ?uniqueValue.
	 *         filter not exists { ?instance emf:isRevisionOf ?revision. }
	 *         filter (?uniqueValue = ?value && ?createdOn = ?minCreatedOn)
	 *         {
	 *             select ?value (min(?createdOn) as ?minCreatedOn)  {
	 *                 ?instance emf:createdOn ?createdOn .
	 *                 ?instance emf:isDeleted "false"^^xsd:boolean .
	 *                 ?instance emf:definitionId ?definitionIdVariable.
	 *                 ?instance ?fieldUriVariable ?value.
	 *                 filter not exists { ?instance emf:isRevisionOf ?revision. }
	 *             } group by ?value
	 *         }
	 *     }
	 * </b></pre>
	 */
	private static final String QUERY_SELECT_OLDEST_INSTANCE = ResourceLoadUtil.loadResource(
			UniqueValueValidationServiceImpl.class, "searchForOldestInstancesOfUniqueFieldValues.sparql");

	/**
	 * Finds all the unique values for the couple "definitionId" and "fieldUri"
	 * and returns the oldest instance for everyone of them. This query have to be used for date values.<br>
	 * Query is:
	 * <pre><b>
	 *     select ?instance ?uniqueValue where {
	 *         ?instance emf:definitionId ?definitionIdVariable.
	 *         ?instance emf:isDeleted "false"^^xsd:boolean.
	 *         ?instance emf:createdOn ?createdOn .
	 *         ?instance ?fieldUriVariable ?uniqueValue.
	 *         filter not exists { ?instance emf:isRevisionOf ?revision. }
	 *         filter (?createdOn = ?minCreatedOn)
	 *         {
	 *             select ?dateValue (min(?createdOn) as ?minCreatedOn)  {
	 *                 ?instance emf:createdOn ?createdOn .
	 *                 ?instance emf:isDeleted "false"^^xsd:boolean .
	 *                 ?instance emf:definitionId ?definitionIdVariable.
	 *                 ?instance ?fieldUriVariable ?value.
	 *                 filter not exists { ?instance emf:isRevisionOf ?revision. }
	 *                 bind(CONCAT(STR(MONTH(?value)), "/", STR(DAY(?value)), "/", STR(YEAR(?value))) as ?dateValue) .
	 *             } group by ?dateValue
	 *         }
	 *     }
	 * </b></pre>
	 */
	private static final String QUERY_SELECT_OLDEST_INSTANCE_FOR_DATE_PROPERTIES = ResourceLoadUtil.loadResource(
			UniqueValueValidationServiceImpl.class, "searchForOldestInstancesOfUniqueFieldDateValues.sparql");

	@Inject
	private DefinitionService definitionService;

	@Inject
	private UniqueValueDao uniqueValueDao;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private UniqueValueLoader uniqueValueLoader;

	//After update DB to PostgreSQL 9.5 this inject can be deleted.
	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private SearchService searchService;

	@Override
	public boolean hasRegisteredValueForAnotherInstance(Instance instance, PropertyDefinition propertyDefinition) {
		Serializable value = instance.get(propertyDefinition.getName());
		String instanceId = (String) instance.getId();
		String definitionId = instance.getIdentifier();
		return hasRegisteredValueForAnotherInstance(instanceId, definitionId, propertyDefinition, value);

	}

	@Override
	public boolean hasRegisteredValueForAnotherInstance(String instanceId, String definitionId,
			PropertyDefinition propertyDefinition, Object value) {
		String fieldUri = propertyDefinition.getUri();
		String convertedPropertyValue = convert(propertyDefinition, value);
		// skip null values.
		if (convertedPropertyValue == null) {
			return false;
		}

		String registeredInstanceId = uniqueValueDao.getInstanceId(definitionId, fieldUri, convertedPropertyValue);
		return registeredInstanceId != null && !registeredInstanceId.equals(instanceId);
	}

	@Override
	public void registerUniqueValue(Instance instance, PropertyDefinition propertyDefinition) {
		String definitionId = instance.getIdentifier();
		Serializable value = instance.get(propertyDefinition.getName());
		String instanceId = (String) instance.getId();

		String convertedPropertyValue = convert(propertyDefinition, value);
		if (convertedPropertyValue == null) {
			// if value is null we have to unregister old record from "sep_unique_values" table.
			uniqueValueDao.unregisterValue(instanceId, definitionId, propertyDefinition.getUri());
		} else {
			uniqueValueDao.registerOrUpdateUniqueValue(instanceId, definitionId, propertyDefinition.getUri(),
					convertedPropertyValue);
		}
	}

	@Override
	public void unRegisterUniqueValues(String instanceId) {
		uniqueValueDao.unregisterAllUniqueValuesForInstance(instanceId);
	}

	@Override
	public void registerUniqueValues(Instance instance) {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		Collection<PropertyDefinition> uniqueFields = extractUniquePropertyDefinitions(Stream.of(instanceDefinition));
		uniqueFields.forEach(propertyDefinition -> registerUniqueValue(instance, propertyDefinition));
	}

	@Override
	public void updateUniqueFields() {
		Set<UniqueField> allRegisteredUniqueFields = getAllRegisteredUniqueFields();
		Set<UniqueField> allDefinedUniqueFieldsFromDefinitions = getAllDefinedUniqueFieldsFromDefinitions();
		Set<UniqueField> toBeUnregistered = getMissing(allRegisteredUniqueFields,
				allDefinedUniqueFieldsFromDefinitions);
		Set<UniqueField> toBeRegistered = getMissing(allDefinedUniqueFieldsFromDefinitions, allRegisteredUniqueFields);
		uniqueValueDao.unRegister(toBeUnregistered);
		uniqueValueDao.register(toBeRegistered);
		uniqueValueLoader.registerUniqueValues(toBeRegistered);
	}

	@Override
	public Collection<PropertyDefinition> extractUniquePropertyDefinitions(Stream<DefinitionModel> definitionModels) {
		return definitionModels
				.flatMap(
						definitionModel -> definitionModel.fieldsStream().filter(PropertyDefinition.isUniqueProperty()))
					.collect(Collectors.toList());
	}

	@Override
	public void registerOldUniqueValues(String definitionId, String fieldUri) {
		Long uniqueFieldId = uniqueValueDao.getUniqueFieldId(definitionId, fieldUri);
		if (uniqueFieldId == null) {
			return;
		}
		getPropertyDefinition(definitionId, fieldUri).ifPresent(
				propertyDefinition -> registerOldUniqueValues(propertyDefinition, definitionId, fieldUri, uniqueFieldId));
	}

	private Optional<PropertyDefinition> getPropertyDefinition(String definitionId, String fieldUri) {
		DefinitionModel definitionModel = definitionService.find(definitionId);
		if (definitionModel == null) {
			return Optional.empty();
		}
		return definitionModel.fieldsStream().filter(PropertyDefinition.hasUri(fieldUri)).findFirst();
	}

	private void registerOldUniqueValues(PropertyDefinition propertyDefinition, String definitionId, String fieldUri, Long uniqueFieldId) {
		List<Pair<String, Serializable>> searchResult = searchForUniqueValuesOfOldestInstances(propertyDefinition, definitionId, fieldUri);
		FragmentedWork.doWork(searchResult, BATCH_SIZE, searchResultBatch -> {
			// After update DB to PostgreSQL 9.5 all of this can be deleted.
			// and just call registerUniqueValuesOfOldestInstances(Collection<Pair<String, Serializable>> couplesInstanceIdValue,
			// PropertyDefinition propertyDefinition, Long uniqueFieldId)
			try {
				// Try to register values of instances as unique with one query. If fail we will do it one
				// by one.
				transactionSupport.invokeInNewTx(() -> registerUniqueValuesOfOldestInstances(searchResultBatch, propertyDefinition, uniqueFieldId));
				return;
			} catch (PersistenceException e) {
				if (isConstraintViolationException(e)) {
					LOGGER.warn("Update unique values with multi instance failed! Will try one by one");
				} else {
					throw e;
				}
			}
			// if insert for all instances fails this mean that some value is registered until registration is in progress
			// so we will try one by one.
			transactionSupport.invokeInNewTx(() -> registerUniqueValuesOfOldestInstancesOneByOne(searchResultBatch, propertyDefinition, uniqueFieldId));
		});
	}

	/**
	 * Searches all different values of couples <code>definitionId</code> and <code>fieldUri</code> and returns oldest
	 * instances for every one of them.
	 *
	 * @return list with pairs. A pair contain the instance id of oldest instance and the value.
	 */
	private List<Pair<String, Serializable>> searchForUniqueValuesOfOldestInstances(PropertyDefinition propertyDefinition, String definitionId, String fieldUri) {
		String query = getUniqueValuesOfOldestInstancesSemanticQuery(propertyDefinition);
		Map<String, Serializable> bindings = new HashMap<>(2);
		bindings.put("definitionIdVariable", definitionId);
		bindings.put("fieldUriVariable", fieldUri);
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setDialect(SearchDialects.SPARQL);
		arguments.setStringQuery(query);
		arguments.setArguments(bindings);
		arguments.setPageSize(0);
		try (Stream<ResultItem> stream = searchService.stream(arguments, ResultItemTransformer.asIs())) {
			return stream.map(
					item -> new Pair<>(item.getString("instance"), item.getResultValue("uniqueValue")))
					.collect(Collectors.toList());
		}
	}

	private String getUniqueValuesOfOldestInstancesSemanticQuery(PropertyDefinition propertyDefinition) {
		if (PropertyDefinition.hasType(DataTypeDefinition.DATE).test(propertyDefinition)) {
			return QUERY_SELECT_OLDEST_INSTANCE_FOR_DATE_PROPERTIES;
		}
		return QUERY_SELECT_OLDEST_INSTANCE;
	}

	/**
	 * Iterates over <code>couplesInstanceIdValue</code> and registers unique values.
	 *
	 * @param couplesInstanceIdValue
	 *         - list with pairs which hold instance id and unique value. {@link Pair#getFirst()} have to return instance id.
	 *         {@link Pair#getSecond()} have to return the value.
	 * @param propertyDefinition
	 *         - property definition of values which will be registered as unique.
	 * @param uniqueFieldId-
	 *         the id of couple definitionId and fieldUri see {@link com.sirma.itt.sep.instance.unique.persistence.UniqueFieldEntity}
	 */
	private void registerUniqueValuesOfOldestInstances(Collection<Pair<String, Serializable>> couplesInstanceIdValue,
			PropertyDefinition propertyDefinition, Long uniqueFieldId) {
		Query nativeQuery = uniqueValueDao.buildNativeInsertUniqueValuesQuery(couplesInstanceIdValue.size(), uniqueFieldId);
		int processedParameters = 1;
		for (Pair<String, Serializable> instance : couplesInstanceIdValue) {
			String instanceId = instance.getFirst();
			String uniqueValue = convert(propertyDefinition, instance.getSecond());
			nativeQuery.setParameter(processedParameters++, uniqueValue);
			nativeQuery.setParameter(processedParameters++, instanceId);
		}
		nativeQuery.executeUpdate();
	}

	/**
	 * Iterates over <code>couplesInstanceIdValue</code>and registers unique values.
	 * Each pair from <code>couplesInstanceIdValue</code> will be processed in separate
	 * transaction because some of them may be already registered while current registration is in process.
	 * After update DB to PostgreSQL 9.5 this method can be deleted.
	 *
	 * @param couplesInstanceIdValue
	 *         - list with pairs which hold instance id and unique value. {@link Pair#getFirst()} have to return instance id.
	 *         {@link Pair#getSecond()} have to return unique value.
	 * @param propertyDefinition
	 *         - property definition of values which will be registered as unique.
	 * @param uniqueFieldId-
	 *         the id of couple definitionId and fieldUri see {@link com.sirma.itt.sep.instance.unique.persistence.UniqueFieldEntity}
	 */
	private void registerUniqueValuesOfOldestInstancesOneByOne(Collection<Pair<String, Serializable>> couplesInstanceIdValue, PropertyDefinition propertyDefinition,
			Long uniqueFieldId) {
		for (Pair<String, Serializable> instance : couplesInstanceIdValue) {
			try {
				transactionSupport.invokeInNewTx(
						() -> registerUniqueValuesOfOldestInstances(Collections.singleton(instance), propertyDefinition, uniqueFieldId));
			} catch (PersistenceException e) {
				if (isConstraintViolationException(e)) {
					LOGGER.warn("Value: {} of instance: {} is already registered.", instance.getSecond(), instance.getFirst());
				} else {
					throw e;
				}
			}
		}
	}

	private String convert(PropertyDefinition propertyDefinition, Object value) {
		if (value == null) {
			return null;
		}
		if (PropertyDefinition.hasType(DataTypeDefinition.DATE).test(propertyDefinition)) {
			return dateFormat.format(typeConverter.convert(Date.class, value));
		}
		return typeConverter.convert(String.class, value);
	}

	/**
	 * Fetches all {@link UniqueField} objects from target which missed in source.
	 */
	private static Set<UniqueField> getMissing(Set<UniqueField> source, Set<UniqueField> target) {
		return source.stream().filter(uniqueField -> !target.contains(uniqueField)).collect(Collectors.toSet());
	}

	private Set<UniqueField> getAllDefinedUniqueFieldsFromDefinitions() {
		return definitionService.getAllDefinitions().flatMap(definitionModel -> {
			String identifier = definitionModel.getIdentifier();
			return definitionModel.fieldsStream().filter(PropertyDefinition.isUniqueProperty()).map(
					propertyDefinition -> new UniqueFieldImpl(identifier, propertyDefinition.getUri()));
		}).collect(Collectors.toSet());
	}

	private Set<UniqueField> getAllRegisteredUniqueFields() {
		return uniqueValueDao
				.getAllUniqueFields()
				.stream()
				.map(uniqueField -> new UniqueFieldImpl(uniqueField.getDefinitionId(), uniqueField.getFieldUri()))
				.collect(Collectors.toSet());
	}

	private boolean isConstraintViolationException(PersistenceException exception) {
		return exception.getCause() instanceof ConstraintViolationException;
	}

	private class UniqueFieldImpl implements UniqueField {
		String definitionId;
		String fieldUri;

		private UniqueFieldImpl(String definitionId, String fieldUri) {
			this.definitionId = definitionId;
			this.fieldUri = fieldUri;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			UniqueFieldImpl that = (UniqueFieldImpl) o;

			if (!definitionId.equals(that.definitionId)) {
				return false;
			}
			return fieldUri.equals(that.fieldUri);
		}

		@Override
		public int hashCode() {
			int result = definitionId.hashCode();
			result = 31 * result + fieldUri.hashCode();
			return result;
		}

		@Override
		public String getFieldUri() {
			return fieldUri;
		}

		@Override
		public String getDefinitionId() {
			return definitionId;
		}
	}
}