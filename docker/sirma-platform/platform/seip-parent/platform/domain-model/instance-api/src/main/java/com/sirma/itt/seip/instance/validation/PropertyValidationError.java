package com.sirma.itt.seip.instance.validation;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Base Class used to contain information for Instance property validation errors. Contains information for the field
 * that is validated and an error message.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public abstract class PropertyValidationError {
	public static final String ERROR_MESSAGE_LABEL_PREFIX = "validation.error.";
	// Normally the validation error messages should be bundled in the definitions. If however those definitions aren't
	// configured correctly this will be used as a default message.
	public static final String DEFAULT_MESSAGE = "Error occurred while validating field with name [%s]. "
			+ "This error message is default, please check your configurations for more precise errors.";

	private PropertyDefinition fieldName;
	private String message;

	public abstract String getValidationType();

	public String getMessage() {
		return message;
	}

	public PropertyValidationError setMessage(String message) {
		this.message = message;
		return this;
	}

	public PropertyDefinition getFieldName() {
		return fieldName;
	}

	public PropertyValidationError setFieldName(PropertyDefinition field) {
		this.fieldName = field;
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder(60)
				.append("{")
				.append("fieldName=").append(fieldName.getName())
				.append(", message='").append(message).append('\'')
				.append('}')
				.toString();
	}
}
