package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Class that holds validation error data for URI fields.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class UriFieldValidationError extends PropertyValidationError {

	UriFieldValidationError(PropertyDefinition field, Function<String, String> messageBuilder) {
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.INVALID_URI;
	}
}
