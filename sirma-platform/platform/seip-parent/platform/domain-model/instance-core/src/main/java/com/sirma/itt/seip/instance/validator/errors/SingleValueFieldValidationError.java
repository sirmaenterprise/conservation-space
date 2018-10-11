package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Object that holds information for failed validations for fields that are not marked as multivalued but a
 * collection was received as a value.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 25/01/2018
 */
public class SingleValueFieldValidationError extends PropertyValidationError {

	SingleValueFieldValidationError(PropertyDefinition field, Function<String, String> messageBuilder) {
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.INVALID_SINGLE_VALUE;
	}
}