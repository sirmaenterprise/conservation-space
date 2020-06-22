package com.sirma.itt.seip.domain.validation;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.validation.ValidationMessage.MessageSeverity;

/**
 * Helper class for translating and interpolating the {@link ValidationMessage} in {@link ValidationReport}.
 * <p>
 * There are two kinds of messages:
 * <ul>
 * <li>
 * Generic error messages - those that are not mapped to concrete node id via {@link ValidationMessage#setNodeId(String)}.
 * To retrieve them use {@link #getGenericErrors}
 * </li>
 * <li>
 * Node error messages - all others, mapped to some node id making them identifiable for which node they are related.
 * To retrieve them use {@link #getNodeErrors}
 * </li>
 * </ul>
 * <p>
 * If the message contains a translation from {@link LabelProvider} it is fetched and interpolated with any present parameters from
 * {@link ValidationMessage#getParams()}. Otherwise it is returned as it is.
 *
 * @author Mihail Radkov
 */
public class ValidationReportTranslator {

	private final LabelProvider labelProvider;
	private final ValidationReport report;

	public ValidationReportTranslator(LabelProvider labelProvider, ValidationReport report) {
		this.labelProvider = labelProvider;
		this.report = report;
	}

	public List<String> getErrors() {
		return translate(report::streamErrorMessages);
	}

	public List<String> getErrors(String language) {
		return translate(report::streamErrorMessages, language);
	}

	public List<String> getGenericErrors() {
		return translate(report::streamGenericMessages);
	}

	public List<String> getGenericErrors(String language) {
		return translate(report::streamGenericMessages, language);
	}

	public Map<String, List<String>> getNodeErrors() {
		return getNodeMessages(MessageSeverity.ERROR);
	}

	public Map<String, List<String>> getNodeErrors(String language) {
		return getNodeMessages(MessageSeverity.ERROR, language);
	}

	public Map<String, List<String>> getNodeMessages(MessageSeverity severity) {
		return report.streamNodeMessages()
				.filter(validationMessage -> validationMessage.getSeverity().equals(severity))
				.collect(Collectors.groupingBy(ValidationMessage::getNodeId,
						Collectors.mapping(this::translate, Collectors.toList())));
	}

	public Map<String, List<String>> getNodeMessages(MessageSeverity severity, String language) {
		return report.streamNodeMessages()
				.filter(validationMessage -> validationMessage.getSeverity().equals(severity))
				.collect(Collectors.groupingBy(ValidationMessage::getNodeId,
						Collectors.mapping(message -> this.translate(message, language), Collectors.toList())));
	}

	private List<String> translate(Supplier<Stream<ValidationMessage>> listSupplier) {
		return listSupplier.get().map(this::translate).collect(Collectors.toList());
	}

	private List<String> translate(Supplier<Stream<ValidationMessage>> listSupplier, String language) {
		return listSupplier.get().map(message -> this.translate(message, language)).collect(Collectors.toList());
	}

	private String translate(ValidationMessage validationMessage) {
		String translation = getTranslation(validationMessage, labelProvider::getLabel);
		return interpolate(validationMessage, translation);
	}

	private String translate(ValidationMessage validationMessage, String language) {
		String translation = getTranslation(validationMessage, labelId -> labelProvider.getLabel(labelId, language));
		return interpolate(validationMessage, translation);
	}

	private String getTranslation(ValidationMessage validationMessage, Function<String, String> labelProvider) {
		String translated = labelProvider.apply(validationMessage.getMessage());
		// Fall back to English
		if (translated == null) {
			return fallbackToEnglish(validationMessage.getMessage());
		}
		return translated;
	}

	private String fallbackToEnglish(String message) {
		String englishTranslation = labelProvider.getLabel(message, Locale.ENGLISH.getLanguage());
		if (englishTranslation != null) {
			return englishTranslation;
		}
		// Message has no translation, return it as it is
		return message;
	}

	private String interpolate(ValidationMessage validationMessage, String message) {
		if (message != null && validationMessage.getParams() != null) {
			return MessageFormat.format(message, validationMessage.getParams());
		}
		return validationMessage.getMessage();
	}
}
