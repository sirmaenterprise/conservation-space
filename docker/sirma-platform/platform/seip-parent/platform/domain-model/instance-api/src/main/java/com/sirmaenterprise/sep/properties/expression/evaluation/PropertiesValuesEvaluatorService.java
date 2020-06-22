package com.sirmaenterprise.sep.properties.expression.evaluation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Service which evaluate properties of given instance.
 *
 * @author Boyan Tonchev.
 */
public interface PropertiesValuesEvaluatorService {

	/**
	 * Evaluate properties of <code>instance</code>.
	 * 
	 * @param instance
	 *            - instances to be processed.
	 * @param propertyNames
	 *            - properties to be processed.
	 * @return map with key -> property name and value -> evaluate property.
	 */
	Map<String, Serializable> evaluate(Instance instance, Collection<String> propertyNames);

	/**
	 * Evaluate single property of <code>instance</code>.
	 * 
	 * @param source
	 *            - definition of processed property.
	 * @param destination
	 *            - definition of received property
	 * @param instance
	 *            - processed instance.
	 * @param propertyName
	 *            - the name of property.
	 * @return evaluated property.
	 */
	Serializable evaluate(PropertyDefinition source, PropertyDefinition destination, Instance instance,
			String propertyName);
}
