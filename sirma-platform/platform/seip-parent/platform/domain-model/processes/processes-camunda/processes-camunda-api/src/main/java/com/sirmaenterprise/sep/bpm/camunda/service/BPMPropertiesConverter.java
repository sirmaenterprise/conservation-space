package com.sirmaenterprise.sep.bpm.camunda.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.variable.VariableMap;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Defines a properties conversion between SEIP model and external process engine model.
 * 
 * @author bbanchev
 */
public interface BPMPropertiesConverter {
	/**
	 * Converts a map of properties to Camunda style map with optional additional processing.
	 * 
	 * @param properties
	 *            is a mandatory parameter that is used as source of conversion.
	 * @param formFields
	 *            is provided filters out only those properties to the result map. Might be null or empty
	 * @return the converted map with optionally reduced set of properties
	 */
	Map<String, Object> convertDataFromSEIPtoCamunda(Map<String, Serializable> properties, List<FormField> formFields);

	/**
	 * Converts a map of properties from Camunda style map with optional additional processing.
	 * 
	 * @param source
	 *            is the source task containing the properties. Might be null
	 * @param destination
	 *            is the destination instance to convert properties for. Might be null
	 * @return the converted map of task properties or null on empty or null source data
	 */
	Map<String, Serializable> convertDataFromCamundaToSEIP(VariableMap source, Instance destination);

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
	static <T> boolean mergeProperties(Map<String, T> target, Map<String, T> source) {
		boolean modified = false;
		for (Map.Entry<String, T> entry : source.entrySet()) {
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

	/**
	 * Put to result map new pair based on {@link PropertyDefinition}.
	 *
	 * @param propertyDefinition - current property definition
	 * @param result - the result map with properties
	 * @param propertyKey - the new pair propertyKey
	 * @param propertyValue - the new pair propertyValue
	 */
	static void appendValue(PropertyDefinition propertyDefinition, Map<String, Serializable> result, String propertyKey,
			Object propertyValue) {
		if (!(propertyValue instanceof Serializable)) {
			return;
		}
		if (propertyDefinition == null) {
			result.putIfAbsent(propertyKey, (Serializable) propertyValue);
			return;
		}
		if (propertyDefinition.isMultiValued().booleanValue()) {
			if (propertyValue instanceof Collection) {
				result.put(propertyKey, (Serializable) propertyValue);
			} else {
				ArrayList<Serializable> newValue = new ArrayList<>(1);
				newValue.add((Serializable) propertyValue);
				result.put(propertyKey, newValue);
			}
			return;
		}
		if (propertyValue instanceof Collection) {
			throw new CamundaIntegrationRuntimeException(
					"Trying to set multi-propertyValue: " + propertyValue + " to single propertyValue property: " + propertyKey);
		}
		result.put(propertyKey, (Serializable) propertyValue);
	}

	/**
	 * Put new value to map and verify that values are not equal.
	 *
	 * @param result - the result map with properties
	 * @param propertyKey - the new pair key
	 * @param propertyValue - the new pair value
	 * @return false only if the previous value associated with key is null or the same as new value
	 */
	static <T> boolean appendValue(Map<String, T> result, String propertyKey, T propertyValue) {
		// override value
		T existing = result.put(propertyKey, propertyValue);
		return !EqualsHelper.nullSafeEquals(existing, propertyValue);
	}
}
