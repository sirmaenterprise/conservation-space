package com.sirma.itt.seip.eai.service.model.transform.impl;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;

/**
 * Abstract converter with default implementation of methods. Intended as a single wrapped CDI instance per each
 * systemId.
 *
 * @author bbanchev
 */
public abstract class DefaultModelConverter implements EAIModelConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TYPE_URI_PATTERN = ":" + DefaultProperties.TYPE;

	private String systemId;

	@Inject
	protected EAIConfigurationService integrationService;
	@Inject
	protected TypeConverter typeConverter;
	@Inject
	protected DefinitionService definitionService;
	@Inject
	protected CodelistService codelistService;

	/**
	 * Instantiates a new default model converter.
	 *
	 * @param systemId
	 *            the system id that this converter is applicable for
	 */
	protected DefaultModelConverter(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public void reset() {
		// nothing to reset as state
	}

	@Override
	public List<Pair<String, Serializable>> convertSEIPtoExternalProperty(String key, Serializable value, String type)
			throws EAIModelException {
		Set<EntityProperty> properties = getEntityPropertyByInternalName(key, type);
		return iterateAndConvertEntityProperties(value, EntityPropertyMapping.AS_DATA, properties);
	}

	private List<Pair<String, Serializable>> iterateAndConvertEntityProperties(Serializable value,
			EntityPropertyMapping usage, Set<EntityProperty> properties) throws EAIModelException {

		List<Pair<String, Serializable>> result = new ArrayList<>(properties.size());
		for (EntityProperty entityProperty : properties) {
			Pair<String, Serializable> pair = new Pair<>(entityProperty.getMapping(usage),
					convertInternalToExternalValue(entityProperty, value));
			result.add(pair);
		}
		return result;
	}

	protected Serializable convertInternalToExternalValue(EntityProperty property, Serializable value)
			throws EAIModelException {
		if (value == null) {
			return null;
		}
		Serializable valueByCodelist;
		// if value is convertible by codelist
		if (property.getCodelist() != null) {
			if (value instanceof Map) {
				valueByCodelist = convertInternalToExternalValueByCodelist(property.getCodelist(),
						(Serializable) ((Map<?, ?>) value).get("id"));
			} else {
				valueByCodelist = convertInternalToExternalValueByCodelist(property.getCodelist(), value);
			}
			if (valueByCodelist == null) {
				throw new EAIModelException("Codelist value " + value + " in codelist " + property.getCodelist()
						+ " is required for property " + property + " but it is missing!");
			}
			return valueByCodelist;
		}
		return value;
	}

	/**
	 * Find and extract source value as codelist entry based on provided codelist number
	 *
	 * @param codelist
	 *            the source codelist
	 * @param source
	 *            the source value
	 * @return the converted value or null if not found
	 */
	protected abstract Serializable convertInternalToExternalValueByCodelist(Integer codelist, Serializable source);

	/**
	 * Find and convert source value as codelist entry value based on provided codelist number
	 *
	 * @param codelist
	 *            the source codelist
	 * @param source
	 *            the source value
	 * @return the converted value or null if not found
	 */
	protected abstract Serializable convertExternalToInternalValueByCodelist(Integer codelist, Serializable source);

	@Override
	public Map<String, Serializable> convertExternaltoSEIPProperties(Map<String, Object> properties, Instance consumer)
			throws EAIModelException {
		return convertExternaltoSEIPProperties(properties, definitionService.getInstanceDefinition(consumer), null);
	}

	/**
	 * Do actual convert of data.
	 *
	 * @param properties
	 *            is the source data
	 * @param instanceDefinition
	 *            is the model of seip target object
	 * @param errorBuilder
	 *            is the error builder to use to store error during processing. Optional parameter - if omitted errors
	 *            are immediately rethrown
	 * @return the converted data as map
	 * @throws EAIModelException
	 *             on errors during processing, as invalid/missing values
	 */
	protected Map<String, Serializable> convertExternaltoSEIPProperties(Map<String, Object> properties,
			DefinitionModel instanceDefinition, ErrorBuilderProvider errorBuilder) throws EAIModelException {
		ModelConfiguration modelConfiguration = getModelConfiguration();
		String type = instanceDefinition.getIdentifier();
		EntityType entityType = modelConfiguration.getTypeByDefinitionId(type);
		if (entityType == null) {
			// critical error
			throw new EAIModelException("Missing data mappings for type: " + type);
		}
		// check if mandatory values presents
		List<EntityProperty> missingMandatory = checkMandatoryValues(entityType, properties);
		if (!missingMandatory.isEmpty()) {
			StringBuilder error = new StringBuilder(256)
					.append("Missing/invalid mandatory properties in data: '")
						.append(missingMandatory.stream().map(EntityProperty::getTitle).collect(Collectors.toList()))
						.append("'");
			if (errorBuilder == null) {
				throw new EAIModelException(error.toString());
			}
			errorBuilder.append(error);
		}
		Map<String, Serializable> value = convertExternaltoSEIPProperties(properties, instanceDefinition,
				modelConfiguration, entityType, errorBuilder);
		if (errorBuilder != null && errorBuilder.hasErrors()) {
			throw new EAIModelException(errorBuilder.toString());
		}
		return value;
	}

	private Map<String, Serializable> convertExternaltoSEIPProperties(Map<String, Object> properties,
			DefinitionModel instanceDefinition, ModelConfiguration modelConfiguration, EntityType entityType,
			ErrorBuilderProvider errorBuilder) throws EAIModelException {
		String type = instanceDefinition.getIdentifier();
		// cache by uri the properties
		Map<String, PropertyDefinition> definitionByUri = instanceDefinition
				.fieldsStream()
					.filter(e -> StringUtils.isNotBlank(e.getUri()) && !"FORBIDDEN".equalsIgnoreCase(e.getUri()))
					.collect(Collectors.toMap(PropertyDefinition::getUri, p -> p));

		Map<String, Serializable> result = new LinkedHashMap<>(properties.size());

		for (Entry<String, Object> entityProperty : properties.entrySet()) {
			EntityProperty propertyByExternalName = modelConfiguration.getPropertyByExternalName(type,
					entityProperty.getKey());
			if (propertyByExternalName == null) {
				LOGGER.error("Missing property mapping: {} of type: {}! Skipping property from model!",
						entityProperty.getKey(), entityType.getIdentifier());
				continue;
			}
			PropertyDefinition propertyDefinition = definitionByUri.get(propertyByExternalName.getUri());
			if (propertyDefinition != null) {
				convertAndAppendValue(result, propertyDefinition, entityProperty.getValue(), errorBuilder);
			} else {
				LOGGER.error("Missing definition id for '{}'! Skipping property from model!",
						propertyByExternalName.getUri());
			}
		}
		return result;
	}

	@Override
	public Pair<String, Serializable> convertExternaltoSEIPProperty(String key, Serializable value, String definitionId)
			throws EAIModelException {
		return convertExternaltoSEIPProperty(key, value, definitionId, null);

	}

	protected Pair<String, Serializable> convertExternaltoSEIPProperty(String key, Serializable value,
			String definitionId, ErrorBuilderProvider errorBuilder) throws EAIModelException {
		EntityProperty propertyByExternalName = getModelConfiguration().getPropertyByExternalName(definitionId, key);
		Map<String, Serializable> convertedValue = Collections.emptyMap();
		if (propertyByExternalName == null) {
			StringBuilder error = new StringBuilder();
			error.append("Missing mapping for property ");
			error.append(key);
			error.append(" in classification ");
			error.append(definitionId);
			if (errorBuilder == null) {
				throw new EAIModelException(error.toString());
			}
			errorBuilder.append(error);
		} else {
			Optional<PropertyDefinition> definition = findInternalFieldForType(definitionId,
					PropertyDefinition.hasUri(propertyByExternalName.getUri()));
			if (definition.isPresent()) {
				convertedValue = new HashMap<>(1);
				convertAndAppendValue(convertedValue, definition.get(), value, errorBuilder);
			} else {
				errorBuilder
						.append("Missing model definition ")
							.append(definitionId)
							.append(" or field with uri ")
							.append(propertyByExternalName.getUri());
			}
		}
		if (errorBuilder != null && errorBuilder.hasErrors()) {
			throw new EAIModelException(errorBuilder.toString());
		}
		if (convertedValue.isEmpty()) {
			return new Pair<>();
		}
		Entry<String, Serializable> next = convertedValue.entrySet().iterator().next();
		return new Pair<>(next.getKey(), next.getValue());
	}

	@Override
	public Optional<PropertyDefinition> findInternalFieldForType(String definitionId,
			Predicate<PropertyDefinition> predicate) {
		DefinitionModel instanceDefinition = definitionService.find(definitionId);
		if (instanceDefinition == null) {
			return Optional.empty();
		}
		// check is present
		return instanceDefinition.findField(predicate);
	}

	/**
	 * Gets the entity property by internal name by querying the {@link ModelConfiguration} for the definitionId and the
	 * propertyId.
	 *
	 * @param key
	 *            the internal uri for property as {@link EntityProperty#getUri()} returns
	 * @param definitionId
	 *            the definition id to look in as {@link EntityType#getIdentifier()} returns
	 * @return the entity properties found by the internal name
	 * @throws EAIModelException
	 *             on missing mapping
	 */
	protected Set<EntityProperty> getEntityPropertyByInternalName(String key, String definitionId)
			throws EAIModelException {
		ModelConfiguration modelConfiguration = getModelConfiguration();
		if (definitionId != null) {
			EntityProperty property = modelConfiguration.getPropertyByInternalName(definitionId, key);
			if (property == null) {
				throw new EAIModelException(
						"Property by name '" + key + "' not found as internal id in definition: " + definitionId + "!");
			}
			return Collections.singleton(property);
		}
		Set<EntityProperty> property = modelConfiguration.getPropertyByInternalName(key);
		if (property == null) {
			throw new EAIModelException("Property by name '" + key + "' not found as internal id!");
		}
		return property;
	}

	/**
	 * Append value to a properties map by initially checking definition model and how to add/override property. If
	 * property is multivalue the new value is added as a collection
	 *
	 * @param appendTo
	 *            where to add the converted key/value
	 * @param definition
	 *            the definition to search the property in
	 * @param value
	 *            the value to convert and append
	 * @throws EAIModelException
	 *             on error during value convert
	 */
	@SuppressWarnings("unchecked")
	protected void convertAndAppendValue(Map<String, Serializable> appendTo, PropertyDefinition definition,
			Object value, ErrorBuilderProvider errorBuilder) throws EAIModelException {
		Serializable valueSerializable = convertExternalToInternalValue(definition, value);
		if (valueSerializable == null) {
			handleInvalidValue(definition, value, errorBuilder);
			return;
		}
		LOGGER.trace("Value for property '{}' is recevied as: '{}' and transformed to: '{}'",
				definition.getIdentifier(), value, valueSerializable);

		String propertyId = definition.getIdentifier();
		if (valueSerializable instanceof Collection) {
			Collection<?> collection = (Collection<?>) appendTo.computeIfAbsent(propertyId, k -> new LinkedList<>());
			collection.addAll((Collection) valueSerializable);
		} else {
			Integer maxLength = definition.getMaxLength();
			if ((valueSerializable instanceof String) && maxLength != null
					&& maxLength.intValue() < String.valueOf(valueSerializable).length()) {
				LOGGER.warn("Received value '{}' for '{}' with length '{}' > max length {}!", valueSerializable,
						definition.getIdentifier(), String.valueOf(valueSerializable).length(), maxLength);
			}
			Serializable oldValue = appendTo.put(propertyId, valueSerializable);
			if (oldValue != null) {
				LOGGER.warn("Overwritting value '{}' for '{}' with '{}'!", oldValue, definition.getIdentifier(),
						valueSerializable);
			}
		}
	}

	@SuppressWarnings("static-method")
	protected void handleInvalidValue(PropertyDefinition definition, Object value, ErrorBuilderProvider errorBuilder)
			throws EAIModelException {
		// differ errors for better logging
		if (definition.getCodelist() != null && value != null) {
			StringBuilder error = new StringBuilder()
					.append("Value for property '")
						.append(definition.getIdentifier())
						.append("' from codelist '")
						.append(definition.getCodelist())
						.append("' is recevied as '")
						.append(value)
						.append("' but no mapping is found! Skipping it!");
			if (errorBuilder == null) {
				throw new EAIModelException(error.toString());
			}
			errorBuilder.append(error);
			return;
		}
		LOGGER.error("Value for property '{}' is recevied but it is null! Skipping it!", definition.getIdentifier());
	}

	/**
	 * Check mandatory values in a list of models and returns the missing data.
	 *
	 * @param entityType
	 *            the entity type to iterate
	 * @param properties
	 *            the properties to check
	 * @return the list of properties or empty if there is no missing properties
	 */
	protected List<EntityProperty> checkMandatoryValues(EntityType entityType, Map<String, Object> properties) {
		// type should be filtered out
		return entityType
				.getProperties()
					.stream()
					.filter(e -> e.isMandatory() && !e.getUri().endsWith(TYPE_URI_PATTERN))
					.filter(e -> isInvalidMandatoryValue(properties, e))
					.collect(Collectors.toList());
	}

	private boolean isInvalidMandatoryValue(Map<String, Object> properties, EntityProperty e) {
		Object value = properties.get(e.getDataMapping());
		if (value == null || !(value instanceof Serializable)) {
			return true;
		}
		if (e.getCodelist() != null) {
			Serializable clValue = convertExternalToInternalValueByCodelist(e.getCodelist(), (Serializable) value);
			if (clValue == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert external to internal value. If the parameter is not serializable type conversion is invoked to
	 * {@link String}
	 *
	 * @param propertyDefinition
	 *            the property definition associated with the value
	 * @param value
	 *            the value as source
	 * @return the serializable value or null if parameter is null
	 * @throws EAIModelException
	 *             on codelist mapping error
	 */
	protected Serializable convertExternalToInternalValue(PropertyDefinition propertyDefinition, Object value)
			throws EAIModelException {
		if (value == null) {
			return null;
		}
		Object localValue = value;
		Integer codelist = propertyDefinition.getCodelist();
		if (codelist != null) {
			if (!(localValue instanceof Serializable)) {
				// last chance convert to String
				localValue = typeConverter.convert(String.class, localValue);
			}
			Map<String, CodeValue> codeValues = codelistService.getCodeValues(codelist);
			if (codeValues == null || codeValues.isEmpty()) {
				throw new EAIModelException("Codelist " + codelist + " is required for field "
						+ propertyDefinition.getIdentifier() + " but is missing!");
			}
			return convertExternalToInternalValueByCodelist(codelist, (Serializable) localValue);
		}
		// check whether multivalue converter is needed
		boolean multiValued = propertyDefinition.isMultiValued() != null
				&& propertyDefinition.isMultiValued().booleanValue();
		if (multiValued) {
			if (!(localValue instanceof Collection)) {
				LOGGER.info("Actual value for property: {} should be multi value, but received single value: {}",
						propertyDefinition.getIdentifier(), localValue);
				localValue = Collections.singletonList(localValue);
			}
		} else if (localValue instanceof Collection) {
			throw new EAIModelException("Actual value for property: " + propertyDefinition.getIdentifier()
					+ " should be single value, but received multi value: " + localValue);
		}
		return convertByDefinition(propertyDefinition, localValue);
	}

	/**
	 * Converts value to {@link Serializable} value specified by the {@link PropertyDefinition} type
	 *
	 * @return the converted value on success
	 * @throws EAIModelException
	 *             in case of fail during conversion
	 */
	protected Serializable convertByDefinition(PropertyDefinition propertyDefinition, Object value)
			throws EAIModelException {
		try {
			if (value instanceof Collection) {
				return (Serializable) typeConverter.convert(propertyDefinition.getDataType().getJavaClass(),
						(Collection<?>) value);
			}
			return (Serializable) typeConverter.convert(propertyDefinition.getDataType().getJavaClass(), value);
		} catch (Exception e) {// NOSONAR
			throw new EAIModelException("Failed to convert value '" + value + "' to "
					+ propertyDefinition.getDataType().getJavaClass() + "! Details:  " + e.getMessage());
		}
	}

	/**
	 * Gets the model configuration for the current client. Value should not be cached since it is different model for
	 * each tenant/client
	 *
	 * @return the model configuration
	 */
	protected ModelConfiguration getModelConfiguration() {
		return integrationService.getIntegrationConfiguration(getName()).getModelConfiguration().get();
	}

	@Override
	public String getName() {
		return systemId;
	}

}