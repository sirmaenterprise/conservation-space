package com.sirma.sep.instance.batch.provisioning;

/**
 * Exception indicating that an error has occurred while provisioning the batch subsystem.
 * 
 * @author nvelkov
 */
public class BatchProvisioningException extends Exception {

	private static final long serialVersionUID = -7091336438390007242L;

	/**
	 * Create a batch provisioning exception.
	 */
	public BatchProvisioningException() {
		super();
	}

	/**
	 * Create a batch provisioning exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            whether suppression should be enabled
	 * @param writableStackTrace
	 *            whether or not the stack trace should be writable
	 */
	public BatchProvisioningException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Create a batch provisioning exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public BatchProvisioningException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a batch provisioning exception.
	 * 
	 * @param message
	 *            the message
	 */
	public BatchProvisioningException(String message) {
		super(message);
	}

	/**
	 * Create a batch provisioning exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public BatchProvisioningException(Throwable cause) {
		super(cause);
	}

}
