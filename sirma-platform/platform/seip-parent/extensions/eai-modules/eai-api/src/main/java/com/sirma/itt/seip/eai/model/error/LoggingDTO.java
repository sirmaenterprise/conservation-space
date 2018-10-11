package com.sirma.itt.seip.eai.model.error;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * The {@link LoggingDTO} holds an error details related to a service failure during communication process with an
 * external system. This error could be sent to the service endpoint for error logging of that system so the error to be
 * registered. The wrapped request contains the severity, the message summary and details, and an origin for the message
 * object. {@link LoggingDTO} could be used as external system wrapped response as well.
 *
 * @author bbanchev
 */
public class LoggingDTO implements ServiceRequest, ServiceResponse {
	private static final long serialVersionUID = 3649365738609398697L;
	@JsonProperty
	private String summary;
	@JsonProperty
	private String details;
	@JsonProperty
	private Serializable origin;
	@JsonProperty
	private InformationSeverity severity = InformationSeverity.ERROR;

	/**
	 * Instantiates a new informative message wrapper
	 */
	public LoggingDTO() {
		// needed by json<->java mapper
		this(null, null);
	}

	/**
	 * Instantiates a new informative message wrapper
	 *
	 * @param summary
	 *            the brief information
	 * @param details
	 *            the detailed information
	 */
	public LoggingDTO(String summary, String details) {
		this.summary = summary;
		this.details = details;
	}

	/**
	 * Getter method for summary.
	 *
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Setter method for summary.
	 *
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Getter method for details.
	 *
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Setter method for details.
	 *
	 * @param details
	 *            the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * Getter method for origin.
	 *
	 * @return the origin
	 */
	public Serializable getOrigin() {
		return origin;
	}

	/**
	 * Setter method for origin - object that could be serialized
	 *
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * Getter method for severity.
	 *
	 * @return the severity
	 */
	public InformationSeverity getSeverity() {
		return severity;
	}

	/**
	 * Setter method for severity.
	 *
	 * @param severity
	 *            the severity to set
	 */
	@JsonSetter(value = "severity")
	public void setSeverity(String severity) {
		if (severity == null) {
			return;
		}
		this.severity = InformationSeverity.valueOf(severity.toUpperCase());
	}

	@JsonIgnore
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Log Message [summary=");
		builder.append(summary);
		builder.append(", details=");
		builder.append(details);
		builder.append(", origin=");
		builder.append(origin);
		builder.append(", severity=");
		builder.append(severity);
		builder.append("]");
		return builder.toString();
	}

}
