package com.sirma.itt.seip.instance.validator.errors;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;

/**
 * Error type that can be used for generic purposes
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/09/2017
 */
public class CustomFieldValidationError extends PropertyValidationError {
	private final String type;

	CustomFieldValidationError(PropertyDefinition field, String type, Function<String, String> messageBuilder) {
		this.type = type;
		setFieldName(field).setMessage(messageBuilder.apply(getValidationType()));
	}

	@Override
	public String getValidationType() {
		return type;
	}
}
