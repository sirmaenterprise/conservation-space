package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Text field validation error that is used when validating the format of a text field in an Instance object fails.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class TextFieldValidationError extends PropertyValidationError {

	TextFieldValidationError(PropertyDefinition field, Function<String, String> errorMessage) {
		setFieldName(field).setMessage(errorMessage.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.INVALID_TEXT_FORMAT;
	}
}
