package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Extracts a date value from a specific field and uses the type converter to format the date object to a formatted date
 * string.
 *
 * @author A. Kunchev
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 70)
public class DatePropertyValueEvaluator implements PropertyValueEvaluator {

	@Inject
	private UserDateConverter dateUtil;

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		return PropertyDefinition.hasType(DataTypeDefinition.DATE).test(source)
				&& PropertyDefinition.hasType(DataTypeDefinition.TEXT).test(destination);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		Serializable value = instance.get(propertyName);
		return value != null ? dateUtil.evaluateDateWithZoneOffset(value) : null;
	}
}
