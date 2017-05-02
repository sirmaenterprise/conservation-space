package com.sirma.itt.seip.configuration;

/**
 * Base exception thrown to indicate problem with configurations loading.
 *
 * @author BBonev
 */
public class ConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 3599008038573983032L;

	/**
	 * Instantiates a new configuration exception.
	 */
	public ConfigurationException() {
		super();
	}

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param message
	 *            the message
	 */
	public ConfigurationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
