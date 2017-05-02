package com.sirma.itt.seip;

import java.io.Serializable;

/**
 * General message wrapper object that provides message info level or severity based on the uses.
 *
 * @author BBonev
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 6761970879817731744L;

	/** The error type. */
	private MessageType messageType;

	/** The message. */
	private String message;

	/**
	 * Create new error message
	 *
	 * @param errorMessage
	 *            the error message
	 * @return the verification message
	 */
	public static Message error(String errorMessage) {
		return new Message(MessageType.ERROR, errorMessage);
	}

	/**
	 * Creates new info message.
	 *
	 * @param infoMessage
	 *            the info message
	 * @return the verification message
	 */
	public static Message info(String infoMessage) {
		return new Message(MessageType.ERROR, infoMessage);
	}

	/**
	 * Creates new warning message
	 *
	 * @param warningMessage
	 *            the warning message
	 * @return the verification message
	 */
	public static Message warning(String warningMessage) {
		return new Message(MessageType.ERROR, warningMessage);
	}

	/**
	 * Instantiates a new validation error impl.
	 *
	 * @param messageType
	 *            the error type
	 * @param message
	 *            the message
	 */
	public Message(MessageType messageType, String message) {
		this.messageType = messageType;
		this.message = message;
	}

	public MessageType getErrorType() {
		return messageType;
	}

	public String getMessage() {
		return message;
	}
}
