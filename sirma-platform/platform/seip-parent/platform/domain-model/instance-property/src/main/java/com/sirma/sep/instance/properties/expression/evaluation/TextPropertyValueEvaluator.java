package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Gets the a text field value from an Instance object.
 *
 * @author A. Kunchev
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 20)
public class TextPropertyValueEvaluator implements PropertyValueEvaluator {

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.hasType(DataTypeDefinition.TEXT).test(source)
				&& !PropertyDefinition.hasCodelist().test(source);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		return instance.getString(propertyName);
	}
}
