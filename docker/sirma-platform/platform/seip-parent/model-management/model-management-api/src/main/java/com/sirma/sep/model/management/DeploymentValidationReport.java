package com.sirma.sep.model.management;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO that carry information about deployment candidates that can be deployed and any validation errors or warnings that occurred
 * during the validation process.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/08/2018
 */
public class DeploymentValidationReport {
	private final List<String> genericErrors = new LinkedList<>();
	private final Map<String, ValidationReportEntry> nodes = new LinkedHashMap<>();

	private long version;

	public List<String> getGenericErrors() {
		return genericErrors;
	}

	public Collection<ValidationReportEntry> getNodes() {
		return nodes.values();
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * Adds the provided errors in the report. Such errors are not bind to specific model node.
	 *
	 * @param errors generic errors to be included in the report
	 */
	public void addGenericErrors(List<String> errors) {
		genericErrors.addAll(errors);
	}

	/**
	 * Register successful validation for the given node. The method do nothing if such node already exists no matter
	 * if successful or not.
	 *
	 * @param id the node id that successfully passed validation
	 */
	public void successfulDeploymentValidationFor(String id) {
		// Simply create if missing
		getOrCreateReportEntry(id);
	}

	/**
	 * Register node for which deployment validation has failed.
	 * <p>
	 * The report entry node will be created if not yet present in the report.
	 *
	 * @param id the identifier of the node that failed the validation
	 * @param errors the validation errors for the given node
	 */
	public void failedDeploymentValidationFor(String id, List<String> errors) {
		ValidationReportEntry reportEntry = getOrCreateReportEntry(id);
		errors.forEach(error -> reportEntry.addReportMessage(new ReportMessage(MessageType.ERROR, error)));
	}

	/**
	 * Register node for which deployment validation has detected warnings.
	 * <p>
	 * The report entry node will be created if not yet present in the report.
	 *
	 * @param id the identifier of the node that has warnings
	 * @param warnings the validation warnings for the given node
	 */
	public void validationWarningFor(String id, List<String> warnings) {
		ValidationReportEntry reportEntry = getOrCreateReportEntry(id);
		warnings.forEach(warning -> reportEntry.addReportMessage(new ReportMessage(MessageType.WARNING, warning)));
	}

	private ValidationReportEntry getOrCreateReportEntry(String id) {
		return nodes.computeIfAbsent(id, ValidationReportEntry::new);
	}

	/**
	 * Checks if the current report has any registered nodes in it
	 *
	 * @return true if no node is registered for successful or failed validation
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(nodes) && CollectionUtils.isEmpty(genericErrors);
	}

	/**
	 * Checks if the report contains any invalid entries. Even if one is present, then the whole report is considered invalid.
	 *
	 * @return <code>true</code> if all report entries are valid, <code>false</code> otherwise
	 */
	public boolean isValid() {
		return nodes.values().stream().noneMatch(node -> !node.isValid()) && CollectionUtils.isEmpty(genericErrors);
	}

	/**
	 * Returns any entries that failed the validation or an empty list.
	 *
	 * @return list with failed {@link ValidationReportEntry}
	 */
	@JsonIgnore
	public List<ValidationReportEntry> getFailedEntries() {
		return nodes.values().stream()
				.filter(reportEntry -> !reportEntry.isValid())
				.collect(Collectors.toList());
	}

	@JsonIgnore
	public List<ValidationReportEntry> getEntriesWithWarnings() {
		return nodes.values().stream()
				.filter(reportEntry -> reportEntry.getMessages()
						.stream()
						.anyMatch(reportMessage -> MessageType.WARNING.equals(reportMessage.getSeverity())))
				.collect(Collectors.toList());
	}

	/**
	 * Returns any entries that succeeded the validation or an empty list.
	 *
	 * @return list with successful {@link ValidationReportEntry}
	 */
	@JsonIgnore
	public List<ValidationReportEntry> getSuccessfulEntries() {
		return nodes.values().stream()
				.filter(ValidationReportEntry::isValid)
				.collect(Collectors.toList());
	}

	/**
	 * Represents a generic validation report entry for specific model node and its validation report messages.
	 *
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 * @since 21/08/2018
	 */
	public static class ValidationReportEntry {
		private final String id;
		private final List<ReportMessage> messages = new LinkedList<>();

		public ValidationReportEntry(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		void addReportMessage(ReportMessage reportMessage) {
			messages.add(reportMessage);
		}

		public boolean isValid() {
			return messages.stream().noneMatch(message -> MessageType.ERROR.equals(message.getSeverity()));
		}

		public List<ReportMessage> getMessages() {
			return messages;
		}

		@Override
		public String toString() {
			return "ValidationReportEntry{" +
					"id='" + id + '\'' +
					", messages=" + messages +
					'}';
		}
	}

	/**
	 * Represents a validation report message with specified severity for the message
	 *
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 * @since 21/08/2018
	 */
	public static class ReportMessage {
		private final MessageType severity;
		private final String message;

		ReportMessage(MessageType severity, String message) {
			this.severity = severity;
			this.message = message;
		}

		public MessageType getSeverity() {
			return severity;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return "ReportMessage{" +
					"severity=" + severity.toString() +
					", message='" + message + '\'' +
					'}';
		}
	}
}
