package com.sirma.itt.seip.domain.validation;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains information about a validation result - a message and it's severity.
 * <p>
 * The validation could be related to concrete node making it identifiable and possible to be mapped.
 * For that use {@link #setNodeId(String)} and {@link #getNodeId()}
 * <p>
 * Additionally it supports parameters related to the message. Usefull if the message itself can be interpolated with these parameters.
 * Use {@link #setParams(Object...)} and {@link #getParams()}.
 *
 * @author Mihail Radkov
 * @see MessageSeverity
 */
public class ValidationMessage {

	private final MessageSeverity severity;

	private final String message;

	private Object[] params;

	private String nodeId;

	public ValidationMessage(MessageSeverity severity, String message) {
		this.severity = severity;
		this.message = message;
	}

	public static ValidationMessage error(String message) {
		return new ValidationMessage(MessageSeverity.ERROR, message);
	}

	public static ValidationMessage error(String nodeId, String message) {
		return new ValidationMessage(MessageSeverity.ERROR, message).setNodeId(nodeId);
	}

	public static ValidationMessage warning(String nodeId, String message) {
		return new ValidationMessage(MessageSeverity.WARNING, message).setNodeId(nodeId);
	}

	public boolean isError() {
		return MessageSeverity.ERROR.equals(getSeverity());
	}

	public boolean hasNodeId() {
		return StringUtils.isNotBlank(nodeId);
	}

	public MessageSeverity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public Object[] getParams() {
		return params;
	}

	public ValidationMessage setParams(Object... params) {
		this.params = params;
		return this;
	}

	public String getNodeId() {
		return nodeId;
	}

	public ValidationMessage setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ValidationMessage that = (ValidationMessage) o;
		return severity == that.severity &&
				Objects.equals(message, that.message) &&
				Arrays.equals(params, that.params) &&
				Objects.equals(nodeId, that.nodeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(severity, message, params, nodeId);
	}

	/**
	 * Supported {@link ValidationMessage} severities.
	 *
	 * @author Mihail Radkov
	 * @see ValidationMessage
	 */
	public enum MessageSeverity {
		WARNING,
		ERROR
	}

}
