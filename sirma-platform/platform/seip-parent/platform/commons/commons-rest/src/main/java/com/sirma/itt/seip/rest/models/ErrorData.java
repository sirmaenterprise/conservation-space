package com.sirma.itt.seip.rest.models;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Error payload used when there is an error in request to the API.
 * @author yasko
 */
public class ErrorData implements Serializable {

	private int code;
	private String message;

	private Map<String, Error> errors = new LinkedHashMap<>();

	/**
	 * Instantiates a new error data.
	 */
	public ErrorData() {
		// default constructor
	}

	/**
	 * Instantiates a new error data.
	 *
	 * @param message
	 *            the message
	 */
	public ErrorData(String message) {
		this.message = message;
	}

	/**
	 * Instantiates a new error data.
	 *
	 * @param code
	 *            the code
	 * @param message
	 *            the message
	 */
	public ErrorData(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code
	 *            the code
	 * @return the error data
	 */
	public ErrorData setCode(int code) {
		this.code = code;
		return this;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message
	 *            the message
	 * @return the error data
	 */
	public ErrorData setMessage(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public Map<String, Error> getErrors() {
		return errors;
	}

	/**
	 * Adds the error.
	 *
	 * @param key
	 *            the key of the error
	 * @param error
	 *            the error to add
	 * @return the error data
	 */
	public ErrorData addError(String key, Error error) {
		if (key != null && error != null) {
			getErrors().put(key, error);
		}
		return this;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors
	 *            the errors
	 * @return the error data
	 */
	public ErrorData setErrors(Map<String, Error> errors) {
		this.errors = errors;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		if (code > 0) {
			builder.append(code).append(", ");
		}
		if (message != null) {
			builder.append(message).append(", ");
		}
		if (errors != null) {
			builder.append("errors=").append(errors);
		}
		builder.append("]");
		return builder.toString();
	}

}
