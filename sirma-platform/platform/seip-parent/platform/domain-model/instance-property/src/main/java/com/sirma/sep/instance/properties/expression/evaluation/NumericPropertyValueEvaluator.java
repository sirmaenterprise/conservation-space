package com.sirma.sep.instance.properties.expression.evaluation;

import static com.sirma.itt.seip.domain.definition.PropertyDefinition.hasType;

import java.io.Serializable;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Retrieves the instance value for numbers.
 *
 * @author A. Kunchev
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 40)
public class NumericPropertyValueEvaluator implements PropertyValueEvaluator {

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return hasType(DataTypeDefinition.INT).or(hasType(DataTypeDefinition.DOUBLE))
				.or(hasType(DataTypeDefinition.FLOAT)).or(hasType(DataTypeDefinition.LONG)).test(source);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		return instance.get(propertyName);
	}

}
