package com.sirmaenterprise.sep.bpm.camunda.actions;

import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * The @{link BPMActionRuntimeException} indicates action runtime exception during execution of action operations.
 * 
 * @author Hristo Lungov
 */
public class BPMActionRuntimeException extends BPMRuntimeException {

	private static final long serialVersionUID = 5249099872546668834L;
	/**
	 * The Label Id will be served to UI where to be diplayed.
	 */
	private final String labelId;

	/**
	 * Instantiates a new Business Process Action runtime exception.
	 *
	 * @param message
	 *            the message
	 * @param labelId
	 *            the lableId to be send to UI
	 * @param cause
	 *            the cause
	 */
	public BPMActionRuntimeException(String message, String labelId, Throwable cause) {
		super(message, cause);
		this.labelId = labelId;
	}

	/**
	 * Instantiates a new Business Process Action runtime exception.
	 *
	 * @param message
	 *            the message
	 * @param labelId
	 *            the lableId to be send to UI
	 */
	public BPMActionRuntimeException(String message, String labelId) {
		super(message);
		this.labelId = labelId;
	}

	/**
	 * Instantiates a new Business Process Action runtime exception.
	 *
	 * @param cause
	 *            the cause
	 * @param labelId
	 *            the lableId to be send to UI
	 */
	public BPMActionRuntimeException(Throwable cause, String labelId) {
		super(cause);
		this.labelId = labelId;
	}

	/**
	 * Gets the action exception label id.
	 * 
	 * @return the prefered label id for this exception.
	 */
	public String getLabelId() {
		return labelId;
	}
}
