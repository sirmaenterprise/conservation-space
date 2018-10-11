package com.sirmaenterprise.sep.properties.value;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertiesValuesEvaluatorService;

/**
 * Rest service to evaluate an expression template. The service takes as input parameters a list object id and property
 * which have to be evaluated. Format of json is {@link PropertiesValuesEvaluatorRequestBodyReader} After evaluation
 * json response will be {@link PropertiesValuesEvaluatorResponseBodyWriter}
 *
 * @author Boyan Tonchev
 */
@Path("/properties/value")
@ApplicationScoped
public class PropertiesValuesEvaluatorRest {

	private static final String DEFAULT_VALUE_PATTERN = "default_value_pattern";

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private PropertiesValuesEvaluatorService propertiesValuesEvaluatorService;

	/**
	 * Evaluate property values. Request hold id and properties names which have to be evaluated.
	 *
	 * @param expressionTemplateRequest
	 *            - object hold needed information for evaluate property values.
	 * @return object with evaluated properties.
	 */
	@POST
	@Path("/eval")
	public PropertiesValuesEvaluatorResponse eval(PropertiesValuesEvaluatorRequest expressionTemplateRequest) {
		Map<Serializable, Instance> instances = loadInstances(expressionTemplateRequest.getInstancesIds());
		DefinitionModel definitionModel = definitionService
				.find(expressionTemplateRequest.getNewInstanceDefinitionId());
		return populateResponse(definitionModel, expressionTemplateRequest, instances);
	}

	/**
	 * Load instances with ids <codea>instanceIds</codea>.
	 *
	 * @param instanceIds
	 *            - instances ids to be loaded.
	 * @return map with key -> instanceId and value instance.
	 */
	private Map<Serializable, Instance> loadInstances(Collection<String> instanceIds) {
		return instanceTypeResolver.resolveInstances(instanceIds).stream()
				.collect(Collectors.toMap(Instance::getId, instance -> instance));
	}

	/**
	 * Populate response data.
	 *
	 * @param definitionModel
	 *            - the definition model of new instance.
	 * @param expressionTemplateRequest
	 *            - model of expression template request.
	 * @param instances
	 *            map with key instance id and value resolved instance.
	 */
	private PropertiesValuesEvaluatorResponse populateResponse(DefinitionModel definitionModel,
			PropertiesValuesEvaluatorRequest expressionTemplateRequest, Map<Serializable, Instance> instances) {
		PropertiesValuesEvaluatorResponse expressionTemplateResponse = new PropertiesValuesEvaluatorResponse();
		expressionTemplateRequest.getExpressionTemplateModelAsStream().forEach(entry -> {
			String instanceId = entry.getKey();
			Instance instance = instances.get(instanceId);
			List<PropertiesValuesEvaluatorProperty> requestedProperties = entry.getValue();
			List<Map<String, Serializable>> instanceProperties = new LinkedList<>();
			requestedProperties.forEach(requestedProperty -> instanceProperties
					.add(extractInstanceProperty(definitionModel, instance, requestedProperty)));
			Map<String, Object> instanceData = new HashMap<>(2);
			instanceData.put(JsonKeys.ID, instanceId);
			instanceData.put(JsonKeys.PROPERTIES, instanceProperties);
			expressionTemplateResponse.addInstanceData(instanceData);
		});
		return expressionTemplateResponse;
	}

	/**
	 * Extract requested property.
	 *
	 * @param definitionModel
	 *            - the definition model of new instance.
	 * @param instance
	 *            - instance which property have to be processed.
	 * @param requestedProperty
	 *            - hold information about requested property.
	 * @return filled map with evaluated requested property.
	 */
	private Map<String, Serializable> extractInstanceProperty(DefinitionModel definitionModel, Instance instance,
			PropertiesValuesEvaluatorProperty requestedProperty) {
		String newInstancePropertyName = requestedProperty.getNewInstancePropertyName();
		String returnInstancePropertyName = requestedProperty.getReturnInstancePropertyName();
		String instancePropertyName = requestedProperty.getInstancePropertyName();
		Map<String, Serializable> responseProperties = new HashMap<>(2);
		responseProperties.put(JsonKeys.PROPERTY_NAME, returnInstancePropertyName);
		responseProperties.put(JsonKeys.PROPERTY_VALUE,
				extractProperty(definitionModel, newInstancePropertyName, instance, instancePropertyName));
		return responseProperties;
	}

	/**
	 * Extract property value of <code>instance</code> with name <code>instancePropertyName</code>
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param newInstancePropertyName
	 *            - property name of new instance where populated data will be used.
	 * @param instance
	 *            - instance from which property value have to be extracted.
	 * @param instancePropertyName
	 *            - the name of property which have to be extracted.
	 * @return extracted value of property.
	 */
	private Serializable extractProperty(DefinitionModel definitionModel, String newInstancePropertyName,
			Instance instance, String instancePropertyName) {
		Serializable propertyValue = instance.get(instancePropertyName);
		if (propertyValue == null) {
			return null;
		}
		if (propertyValue instanceof Collection<?>) {
			return processMultiValue(definitionModel, newInstancePropertyName, instance, instancePropertyName,
					propertyValue);
		}
		return processSingleValue(definitionModel, newInstancePropertyName, instance, instancePropertyName,
				propertyValue);
	}

	/**
	 * Process single value property.If new instance property is single valued we return evaluated value otherwise
	 * return property as collection of one element.
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param newInstancePropertyName
	 *            - property name of new instance where populated data will be used.
	 * @param instance
	 *            - instance from which property value have to be extracted.
	 * @param instancePropertyName
	 *            - the name of property which have to be extracted.
	 * @param propertyValue
	 *            - value of property.
	 * @return the value of property.
	 */
	private Serializable processSingleValue(DefinitionModel definitionModel, String newInstancePropertyName,
			Instance instance, String instancePropertyName, Serializable propertyValue) {
		Optional<PropertyDefinition> newPropertyDefinitionName = definitionModel.getField(newInstancePropertyName);
		if (newPropertyDefinitionName.isPresent()) {
			if (newPropertyDefinitionName.get().isMultiValued()) {
				Serializable serializable = evaluateValue(definitionModel, newInstancePropertyName, instance,
						instancePropertyName, propertyValue);
				return (Serializable) Collections.singleton(serializable);
			}
			return evaluateValue(definitionModel, newInstancePropertyName, instance, instancePropertyName,
					propertyValue);
		}
		return null;
	}

	/**
	 * Process multi value property.
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param newInstancePropertyName
	 *            - property name of new instance where populated data will be used.
	 * @param instance
	 *            - instance from which property value have to be extracted.
	 * @param instancePropertyName
	 *            - the name of property which have to be extracted.
	 * @param propertyValue
	 *            - value of property.
	 * @return the value of property.
	 */
	private Serializable processMultiValue(DefinitionModel definitionModel, String newInstancePropertyName,
			Instance instance, String instancePropertyName, Serializable propertyValue) {
		if (haveToProcessMultiValuedPropertyValue(definitionModel, newInstancePropertyName)) {
			return evaluateValue(definitionModel, newInstancePropertyName, instance, instancePropertyName,
					propertyValue);
		}
		return null;
	}

	/**
	 * Check if property have to be evaluated ({@link PropertiesValuesEvaluatorRest#haveToEvaluateProperties}). If yes
	 * evaluate it otherwise return as it is.
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param newInstancePropertyName
	 *            - property name of new instance where populated data will be used.
	 * @param instance
	 *            - instance from which property value have to be extracted.
	 * @param instancePropertyName
	 *            - the name of property which have to be extracted.
	 * @param propertyValue
	 *            - value of property.
	 * @return the value of property.
	 */
	private Serializable evaluateValue(DefinitionModel definitionModel, String newInstancePropertyName,
			Instance instance, String instancePropertyName, Serializable propertyValue) {
		if (haveToEvaluateProperties(definitionModel, newInstancePropertyName)) {
			DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
			Optional<PropertyDefinition> sourceField = instanceDefinition.getField(instancePropertyName);
			Optional<PropertyDefinition> destinationField = definitionModel.getField(newInstancePropertyName);
			if (sourceField.isPresent()) {
				return propertiesValuesEvaluatorService.evaluate(sourceField.get(), destinationField.get(), instance,
						instancePropertyName);
			}
		}
		return propertyValue;
	}

	/**
	 * Check if we can add multi value to single value. For now if destination is text field ("default_value_pattern"
	 * control) we can process multi value result.
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param propertyName
	 *            - property name of property to be checked.
	 * @return true if we can process multi value.
	 */
	private static boolean haveToProcessMultiValuedPropertyValue(DefinitionModel definitionModel, String propertyName) {
		Optional<PropertyDefinition> targetPropertyDefinition = definitionModel.getField(propertyName);
		// check if target is single valued we can't add many values to it.
		return targetPropertyDefinition.isPresent() && (targetPropertyDefinition.get().isMultiValued()
				|| isDefaultValueControl(targetPropertyDefinition.get())
						&& !PropertyDefinition.hasCodelist().test(targetPropertyDefinition.get()));
	}

	/**
	 * Check property with <code>propertyName</code> of new instance definition if its definition is
	 * "default_value_pattern" but not code list we have to evaluate property value not just return it.
	 *
	 * @param definitionModel
	 *            - definition model of new instance.
	 * @param propertyName
	 *            the name of property to be processed.
	 * @return true if property have to be evaluated.
	 */
	private static boolean haveToEvaluateProperties(DefinitionModel definitionModel, String propertyName) {
		Optional<PropertyDefinition> fieldPropertyDefinition = definitionModel.getField(propertyName);
		if (fieldPropertyDefinition.isPresent()) {
			PropertyDefinition propertyDefinition = fieldPropertyDefinition.get();
			return isDefaultValueControl(propertyDefinition)
					&& !PropertyDefinition.hasCodelist().test(propertyDefinition);
		}
		return false;
	}

	/**
	 * Check if property has type "default_value_pattern".
	 *
	 * @param propertyDefinition
	 *            - definition of property to be checked.
	 * @return true if property has type "default_value_pattern".
	 */
	private static boolean isDefaultValueControl(PropertyDefinition propertyDefinition) {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		return controlDefinition != null && controlDefinition.paramsStream()
				.filter(param -> DEFAULT_VALUE_PATTERN.equalsIgnoreCase(param.getType())).count() > 0;
	}
}