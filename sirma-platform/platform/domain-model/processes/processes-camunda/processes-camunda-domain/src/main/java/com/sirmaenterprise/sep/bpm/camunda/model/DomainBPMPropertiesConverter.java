package com.sirmaenterprise.sep.bpm.camunda.model;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;

/**
 * Customized {@link BPMPropertiesConverter} that converts all non-filtered properties to a Camunda specific format with
 * respect of SEP property type
 *
 * @author bbanchev
 */
@Singleton
public class DomainBPMPropertiesConverter implements BPMPropertiesConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TypeConverter typeConverter;
	@Inject
	private DictionaryService dictionaryService;

	@Override
	public Map<String, Object> convertDataFromSEIPtoCamunda(Map<String, Serializable> properties,
			List<FormField> formFields) {
		Map<String, Object> converted;
		if (formFields != null && !formFields.isEmpty()) {
			converted = new LinkedHashMap<>(formFields.size());
			formFields.forEach(field -> processFieldEntry(field, converted, properties));
		} else {
			converted = new LinkedHashMap<>(properties.size());
			properties.entrySet().forEach(entry -> processGenericEntry(converted, entry.getKey(), entry.getValue()));
		}
		return converted;
	}

	private void processGenericEntry(Map<String, Object> converted, String key, Serializable value) {
		// store sample types as provided
		if (value instanceof String || value instanceof Date || value instanceof Number) {
			converted.put(key, value);
		} else if (value instanceof Collection || value instanceof Map) {
			converted.put(key, value);
		} else if (value != null) {
			converted.put(key, typeConverter.convert(String.class, value));
		}
	}

	private void processFieldEntry(FormField field, Map<String, Object> converted,
			Map<String, Serializable> properties) {
		processGenericEntry(converted, field.getId(), properties.get(field.getId()));
	}

	@Override
	public Map<String, Serializable> convertDataFromCamundaToSEIP(VariableMap source, Instance target) {
		if (source == null || source.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, PropertyDefinition> model = null;
		if (target != null) {
			DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(target);
			if (instanceDefinition != null) {
				model = instanceDefinition.getFieldsAsMap();
			} else {
				LOGGER.warn("Definition model not found for provided instance {}", target.getId());
			}
		}
		Map<String, Serializable> converted = new HashMap<>(source.size() + 3);
		for (String propertyId : source.keySet()) {
			// skip non model properties
			if (model != null && !model.containsKey(propertyId)) {
				continue;
			}
			TypedValue valueTyped = source.getValueTyped(propertyId);
			Object value = valueTyped.getValue();
			if (model != null) {
				appendValue(model.get(propertyId), converted, propertyId, value);
			} else {
				appendValue(null, converted, propertyId, value);
			}
		}
		return converted;
	}

	private static void appendValue(PropertyDefinition propertyDefinition, Map<String, Serializable> result, String key,
			Object value) {
		if (!(value instanceof Serializable)) {
			LOGGER.warn("Provided value for BPM conversion is not a Serializable: {}. Skipping it from result model!",
					value);
			return;
		}
		if (propertyDefinition == null) {
			result.putIfAbsent(key, (Serializable) value);
			return;
		}
		if (propertyDefinition.isMultiValued().booleanValue()) {
			if (value instanceof Collection) {
				result.put(key, (Serializable) value);
			} else {
				ArrayList<Serializable> newValue = new ArrayList<>(1);
				newValue.add((Serializable) value);
				result.put(key, newValue);
			}
			return;
		}
		if (value instanceof Collection) {
			throw new CamundaIntegrationRuntimeException(
					"Trying to set multi-value: " + value + " to single value property: " + key);
		}
		result.put(key, (Serializable) value);
	}

	/**
	 * Merges a source map to target map using the following algorithm
	 * <ul>
	 * <li>If value from source map is collection:
	 * <ul>
	 * <li>if value in target is collection all non duplicate entries are added</li>
	 * <li>if value in target is null collection is added as it is</li>
	 * <li>if value in target is non null exception is generated</li>
	 * </ul>
	 * <li>All other values are stored as is in the target</li>
	 * </ul>
	 * 
	 * @param target
	 *            is the target map to merge properties in.
	 * @param source
	 *            is the map of source values to merge in target
	 * @return whether the target map is modified at all
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> boolean mergeProperties(Map<String, T> target, Map<String, T> source) {
		boolean modified = false;
		for (Entry<String, T> entry : source.entrySet()) {
			String propertyId = entry.getKey();
			T propertyValue = entry.getValue();
			// collections are explicitly processed by specification
			if (propertyValue instanceof Collection) {
				Collection collection = (Collection) propertyValue;
				T oldValue = target.get(propertyId);
				if (oldValue instanceof Collection) {
					// remove the duplicates from the new value
					collection.removeAll((Collection) oldValue);
					modified |= !collection.isEmpty();
					// add all new values
					((Collection) oldValue).addAll(collection);
					continue;
				} else if (oldValue != null) {
					throw new CamundaIntegrationRuntimeException(
							"Incompatible value is going to be stored in: " + propertyId + ", value: " + propertyValue);
				}
				modified |= appendValue(target, propertyId, propertyValue);
			} else {
				modified |= appendValue(target, propertyId, propertyValue);
			}
		}
		return modified;
	}

	private static <T> boolean appendValue(Map<String, T> target, String propertyId, T propertyValue) {
		// override value
		T existing = target.put(propertyId, propertyValue);
		return !EqualsHelper.nullSafeEquals(existing, propertyValue);
	}
}
