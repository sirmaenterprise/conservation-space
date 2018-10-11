package com.sirma.sep.instance.properties.expression.evaluation;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertiesValuesEvaluatorService;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Evaluate property of instance.
 *
 * @author Boyan Tonchev
 * @author A. Kunchev
 */
@ApplicationScoped
public class PropertiesValuesEvaluatorServiceImpl implements PropertiesValuesEvaluatorService {

	@Inject
	@ExtensionPoint(PropertyValueEvaluator.PLUGIN_NAME)
	private Plugins<PropertyValueEvaluator> propertyEvaluators;

	@Inject
	private DefinitionService definitionService;

	@Override
	public Map<String, Serializable> evaluate(Instance instance, Collection<String> propertyNames) {
		DefinitionModel definition = definitionService.getInstanceDefinition(instance);
		if (definition == null) {
			throw new EmfRuntimeException("Could not load definition model for instance with id: " + instance.getId());
		}

		return propertyNames
				.stream()
					.collect(toMap(identity(), property -> evaluateProperty(instance, definition, property)));
	}

	private Serializable evaluateProperty(Instance instance, DefinitionModel instanceDefinition, String propertyName) {
		Optional<PropertyDefinition> field = instanceDefinition.getField(propertyName);
		return field.map(propertyDefinition -> evaluate(propertyDefinition, null, instance, propertyName)).get();
	}

	@Override
	public Serializable evaluate(PropertyDefinition source, PropertyDefinition destination, Instance instance,
			String propertyName) {
		return propertyEvaluators
				.stream()
					.filter(evaluator -> evaluator.canEvaluate(source, destination))
					.findFirst()
					.map(evaluator -> evaluator.evaluate(instance, propertyName))
					.orElse("");
	}
}
