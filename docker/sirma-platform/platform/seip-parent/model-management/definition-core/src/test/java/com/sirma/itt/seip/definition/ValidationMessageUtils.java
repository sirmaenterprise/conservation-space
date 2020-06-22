package com.sirma.itt.seip.definition;

import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.domain.validation.ValidationMessage;

public class ValidationMessageUtils {

	public static boolean hasError(List<ValidationMessage> errors, String error) {
		return errors.stream().anyMatch(errorMessage -> errorMessage.getMessage().equals(error));
	}

	public static boolean hasError(List<ValidationMessage> errors, String error, Object... params) {
		return errors.stream()
				.filter(msg -> msg.getMessage().equals(error))
				.filter(validationMessage -> Arrays.equals(validationMessage.getParams(), params))
				.findFirst()
				.isPresent();
	}

	public static ValidationMessage error(String identifier, String errorCode, Object... params) {
		if (params == null || params.length == 0) {
			return ValidationMessage.error(identifier, errorCode);
		}
		return ValidationMessage.error(identifier, errorCode).setParams(params);
	}
}
