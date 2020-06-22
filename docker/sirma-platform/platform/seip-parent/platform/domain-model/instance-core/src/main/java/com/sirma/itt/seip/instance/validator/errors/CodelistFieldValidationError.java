package com.sirma.itt.seip.instance.validator.errors;

import java.util.Map;
import java.util.function.Function;

import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;

/**
 * Class used to hold the error details for code list field.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class CodelistFieldValidationError extends PropertyValidationError {

	private Map<String, CodeValue> validCodeValues;

	CodelistFieldValidationError(PropertyDefinition field,
			Function<String, String> messageBuilder, Map<String, CodeValue> validCodeValues) {
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
		this.validCodeValues = validCodeValues;
	}

	/**
	 * @return the code values valid for this property
	 */
	public Map<String, CodeValue> getValidCodeValues() {
		return validCodeValues;
	}

	@Override
	public String getValidationType() {
		return PropertyValidationErrorTypes.INVALID_CODELIST;
	}
}
