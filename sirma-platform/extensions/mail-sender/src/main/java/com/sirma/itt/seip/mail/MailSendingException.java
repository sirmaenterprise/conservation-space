package com.sirma.itt.seip.mail;

/**
 * Thrown when the mail sending fails for a known reason.
 *
 * @author Adrian Mitev
 */
public class MailSendingException extends RuntimeException {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 6737972788833971822L;

	private final MailSendingErrorType errorType;

	/**
	 * Initializes the exception data.
	 *
	 * @param errorType
	 *            error type.
	 * @param cause
	 *            exception cause.
	 */
	public MailSendingException(MailSendingErrorType errorType, Throwable cause) {
		super(cause);
		this.errorType = errorType;
	}

	/**
	 * Initializes the exception datga.
	 *
	 * @param errorType
	 *            error type.
	 * @param message
	 *            unwrapped exception message.
	 * @param cause
	 *            exception cause.
	 */
	public MailSendingException(MailSendingErrorType errorType, String message, Throwable cause) {
		super(message, cause);
		this.errorType = errorType;
	}

	/**
	 * Represents the available errors that may occur during mail sending.
	 *
	 * @author Adrian Mitev
	 */
	public enum MailSendingErrorType {
		AUTHENTICATION_FAILED, INVALID_ADDRESS, CONNECTION_FAILED, UKNOWN_RECEPIENT, UKNOWN_REASON
	}

	/**
	 * Getter method for errorType.
	 *
	 * @return the errorType
	 */
	public MailSendingErrorType getErrorType() {
		return errorType;
	}

}
