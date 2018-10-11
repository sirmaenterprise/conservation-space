package com.sirma.sep.instance.properties.expression.evaluation;

import java.io.Serializable;
import java.util.function.Predicate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.properties.expression.evaluation.PropertyValueEvaluator;

/**
 * Extracts a date value from a specific field.
 *
 * @author S.Djulgerova
 */
@Extension(target = PropertyValueEvaluator.PLUGIN_NAME, order = 10)
public class DateToDateValueEvaluator implements PropertyValueEvaluator {

	@Override
	public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
		Predicate<PropertyDefinition> isDateTime = PropertyDefinition.hasType(DataTypeDefinition.DATETIME)
				.or(PropertyDefinition.hasType(DataTypeDefinition.DATE));
		return isDateTime.test(source) && isDateTime.test(destination);
	}

	@Override
	public Serializable evaluate(Instance instance, String propertyName) {
		Serializable value = instance.get(propertyName);
		if (value == null) {
			return null;
		}
		return new DateTime(value).withZone(DateTimeZone.UTC).toString();
	}
}
