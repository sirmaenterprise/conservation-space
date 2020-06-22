package com.sirmaenterprise.sep.bpm.camunda.service;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Customized {@link BPMPropertiesConverter} that converts all non-filtered properties to a Camunda specific format with
 * respect of SEP property type.
 *
 * @author bbanchev
 */
@Singleton
public class BPMPropertiesConverterImpl implements BPMPropertiesConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private TypeConverter typeConverter;
	@Inject
	private DefinitionService definitionService;

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
			DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(target);
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
				BPMPropertiesConverter.appendValue(model.get(propertyId), converted, propertyId, value);
			} else {
				BPMPropertiesConverter.appendValue(null, converted, propertyId, value);
			}
		}
		return converted;
	}
}
