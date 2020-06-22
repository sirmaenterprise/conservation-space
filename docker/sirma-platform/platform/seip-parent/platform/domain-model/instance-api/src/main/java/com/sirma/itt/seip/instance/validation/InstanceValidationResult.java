package com.sirma.itt.seip.instance.validation;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data object that is used to pass validation result data for instances. It contains a list of errors if there were
 * such during the validation process
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
public class InstanceValidationResult {

	private final List<PropertyValidationError> errorMessages;

	/**
	 * Constructor.
	 *
	 * @param errors
	 * 		list of errors which is used to create and unmodifiable collection that stores the validation
	 * 		errors if any.
	 */
	public InstanceValidationResult(List<PropertyValidationError> errors) {
		errorMessages = Collections.unmodifiableList(errors);
	}

	/**
	 * Gets validation errors by their types from {@link PropertyValidationErrorTypes}.
	 *
	 * @param type
	 * 		value from {@link PropertyValidationErrorTypes}.
	 * @return a list of properties.
	 */
	public List<PropertyValidationError> getErrorsByType(String type) {
		return errorMessages.stream()
				.filter(error -> nullSafeEquals(error.getValidationType(), type))
				.collect(Collectors.toList());
	}

	public List<PropertyValidationError> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * Checks if the instance validations has passed.
	 *
	 * @return true if there are no validation errors, false otherwise.
	 */
	public boolean hasPassed() {
		return errorMessages.isEmpty();
	}

	@Override
	public String toString() {
		return new StringBuilder(30)
				.append("InstanceValidationResult{")
				.append("passed=").append(hasPassed())
				.append(", errorMessages=").append(errorMessages)
				.append('}')
				.toString();
	}
}
