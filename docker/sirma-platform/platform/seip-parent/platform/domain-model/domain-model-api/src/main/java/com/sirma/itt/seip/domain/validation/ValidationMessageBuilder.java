package com.sirma.itt.seip.domain.validation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for creating multiple {@link ValidationMessage} and building a {@link ValidationReport} out of them.
 * <p>
 * To be extended for specific validation processes.
 *
 * @author Mihail Radkov
 */
public abstract class ValidationMessageBuilder {

	private List<ValidationMessage> messages = new LinkedList<>();

	protected ValidationMessageBuilder add(ValidationMessage message) {
		messages.add(message);
		return this;
	}

	protected ValidationMessage error(String identifier, String error, Object... params) {
		ValidationMessage errorMessage = ValidationMessage.error(identifier, error).setParams(params);
		add(errorMessage);
		return errorMessage;
	}

	protected ValidationMessage warning(String identifier, String warning, Object... params) {
		ValidationMessage warningMessage = ValidationMessage.warning(identifier, warning).setParams(params);
		add(warningMessage);
		return warningMessage;
	}

	public List<ValidationMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public ValidationReport build() {
		return new ValidationReport(messages);
	}

}