package com.sirmaenterprise.sep.properties.expression.evaluation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Interface for property value evaluators. All class that implement this interface will evaluate a property value. For
 * example if we have code list evaluator it can fetch code value.
 *
 * @author Boyan Tonchev.
 */
public interface PropertyValueEvaluator extends Plugin { // NOSONAR

	/**
	 * PropertyResolver Plugin extensions name
	 */
	String PLUGIN_NAME = "PropertyValueEvaluator";

	/**
	 * Check if can evaluate property.
	 * 
	 * @param source
	 *            - definition of processed property.
	 * @param destination
	 *            - definition of received property
	 * @return - true if current evaluator can evaluate property.
	 */
	boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination);

	/**
	 * Evaluate property with <code>propertyName</code>.
	 * 
	 * @param instance
	 *            - instance which property have to be processed.
	 * @param propertyName
	 *            - the name of property.
	 * @return evaluated property.
	 */
	Serializable evaluate(Instance instance, String propertyName);

	/**
	 * Get resolved instances as stream
	 * 
	 * @param instance
	 *            - instance which property have to be processed.
	 * @param propertyName
	 *            - processed property name
	 * @param instanceTypeResolver
	 *            - type resolver
	 * @return Stream of resolved instances
	 */
	default Stream<Instance> getInstances(Instance instance, String propertyName,
			InstanceTypeResolver instanceTypeResolver) {
		Serializable propertyValue = instance.get(propertyName);
		Collection<String> objectsIds = Collections.emptyList();
		if (propertyValue instanceof String) {
			objectsIds = Collections.singleton((String) propertyValue);
		} else if (propertyValue instanceof Collection) {
			// noinspection unchecked
			objectsIds = (List<String>) propertyValue;
		}
		return instanceTypeResolver.resolveInstances(objectsIds).stream();
	}
}
