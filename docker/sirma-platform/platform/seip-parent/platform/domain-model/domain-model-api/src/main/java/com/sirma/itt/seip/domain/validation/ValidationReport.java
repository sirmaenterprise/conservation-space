package com.sirma.itt.seip.domain.validation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.validation.ValidationMessage.MessageSeverity;

/**
 * A report containing {@link ValidationMessage} that resulted from some validation phase.
 * <p>
 * It provides means for adding messages, retrieving messages and merging other reports.
 *
 * @author Mihail Radkov
 */
public class ValidationReport {

	private final List<ValidationMessage> messages;

	public ValidationReport() {
		this.messages = new LinkedList<>();
	}

	public ValidationReport(List<ValidationMessage> messages) {
		this.messages = messages;
	}

	public static ValidationReport valid() {
		return new ValidationReport();
	}

	public ValidationReport addMessage(ValidationMessage message) {
		messages.add(message);
		return this;
	}

	public ValidationReport addMessages(List<ValidationMessage> messages) {
		messages.forEach(this::addMessage);
		return this;
	}

	public ValidationReport addError(String error) {
		messages.add(ValidationMessage.error(error));
		return this;
	}

	public ValidationReport addErrors(List<String> errors) {
		errors.forEach(error -> messages.add(ValidationMessage.error(error)));
		return this;
	}

	public boolean isValid() {
		return messages.stream().noneMatch(message -> MessageSeverity.ERROR.equals(message.getSeverity()));
	}

	public List<ValidationMessage> getValidationMessages() {
		return Collections.unmodifiableList(messages);
	}

	public Stream<ValidationMessage> streamErrorMessages() {
		return messages.stream().filter(ValidationMessage::isError);
	}

	public Stream<ValidationMessage> streamGenericMessages() {
		return streamErrorMessages().filter(message -> !message.hasNodeId());
	}

	public Stream<ValidationMessage> streamNodeMessages() {
		return messages.stream().filter(ValidationMessage::hasNodeId);
	}

	public List<String> getErrors() {
		return streamErrorMessages().map(ValidationMessage::getMessage).collect(Collectors.toList());
	}

	public void merge(ValidationReport validationReport) {
		addMessages(validationReport.getValidationMessages());
	}
}
