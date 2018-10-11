package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Evaluator implementation for boolean properties. It will get the boolean value for the specified property as string
 * or it will return <code>null</code>, if there is no value mapped to the specifie property.
 *
 * @author A. Kunchev
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 80)
public class BooleanPropertyValueEvaluator implements PropertyValueEvaluator {

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.hasType(DataTypeDefinition.BOOLEAN).test(source);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		return instance.getAsString(propertyName);
	}

}
