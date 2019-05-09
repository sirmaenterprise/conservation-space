package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Object that holds information for failed mandatory control param validation.
 *
 * @author Stella Djulgerova
 */
public class MandatoryControlParamValidationError extends PropertyValidationError {

	MandatoryControlParamValidationError(PropertyDefinition field, Function<String, String> messageBuilder) {
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.MISSING_MANDATORY_CONTROL_PARAM;
	}
}
