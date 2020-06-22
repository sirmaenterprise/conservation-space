package com.sirmaenterprise.sep.bpm.camunda.service;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * Defines the current {@link ProcessEngine} status with optional message for each status. Use {@link #equals(Object)}
 * or {@link #compareTo(ProcessEngineStatus)} to compare values.
 *
 * @author bbanchev
 */
public class ProcessEngineStatus implements Comparable<ProcessEngineStatus>, Cloneable {
	/**
	 * Defines the possible statuses of Camunda {@link ProcessEngine}
	 *
	 * @author bbanchev
	 */
	public enum ProcessEngineStatusValue {
		/** Configured and services are ready to use. */
		AVAILABLE,
		/** Not configured/activated and services won't be available. */
		UNAVAILABLE,
		/** Error during status check or {@link ProcessEngine} check. */
		ERROR,
		/** Detected invalid request - as accessing {@link ProcessEngine} in system tenant. */
		INVALID_REQUEST;
	}

	/** Value for {@link ProcessEngineStatusValue#AVAILABLE} */
	public static final ProcessEngineStatus AVAILABLE = new ProcessEngineStatus(ProcessEngineStatusValue.AVAILABLE);
	/** Value for {@link ProcessEngineStatusValue#UNAVAILABLE} */
	public static final ProcessEngineStatus UNAVAILABLE = new ProcessEngineStatus(ProcessEngineStatusValue.UNAVAILABLE);
	/** Value for {@link ProcessEngineStatusValue#ERROR} */
	public static final ProcessEngineStatus ERROR = new ProcessEngineStatus(ProcessEngineStatusValue.ERROR);
	/** Value for {@link ProcessEngineStatusValue#INVALID_REQUEST} */
	public static final ProcessEngineStatus INVALID_REQUEST = new ProcessEngineStatus(
			ProcessEngineStatusValue.INVALID_REQUEST);

	private ProcessEngineStatusValue status;
	/** custom message */
	private String message = "";

	private ProcessEngineStatus(ProcessEngineStatusValue status) {
		this.status = status;
	}

	/**
	 * Builds custom status with the provided message
	 *
	 * @param customMessage
	 *            is the message to set
	 * @return custom status
	 */
	public ProcessEngineStatus withMessage(String customMessage) {
		ProcessEngineStatus clone = new ProcessEngineStatus(status);
		clone.message = customMessage;
		return clone;
	}

	/**
	 * Gets the status value code
	 *
	 * @return the status value code
	 */
	public ProcessEngineStatusValue getValue() {
		return status;
	}

	/**
	 * Gets the custom message related to this status
	 *
	 * @return the custom message - by default empty message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public int compareTo(ProcessEngineStatus o) {
		return status.compareTo(o.status);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ProcessEngineStatus other = (ProcessEngineStatus) obj;
		if (status != other.status) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ProcessEngineStatus [status=" + status + ", message=" + message + "]";
	}
}