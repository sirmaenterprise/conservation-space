package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Numeric validation error. Build two types of errors.
 * <p/>
 * <ol>
 * <li>One for wrong data type</li>
 * <li>One for wrong format, i.e. number with length 10 digits.</li>
 * </ol>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class NumericFieldValidationError extends PropertyValidationError {

	NumericFieldValidationError(PropertyDefinition field, Function<String, String> messageBuilder) {
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.INVALID_NUMBER;
	}
}
