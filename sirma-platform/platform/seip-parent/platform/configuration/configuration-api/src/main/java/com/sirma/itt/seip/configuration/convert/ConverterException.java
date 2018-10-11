package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.configuration.ConfigurationException;

/**
 * Exception thrown by {@link ConfigurationValueConverter} to notify for conversion error that cannot be handled
 *
 * @author BBonev
 */
public class ConverterException extends ConfigurationException {
	private static final long serialVersionUID = 7231921033910828031L;

	/**
	 * Instantiates a new converter exception.
	 *
	 * @param message
	 *            the message
	 */
	public ConverterException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new converter exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public ConverterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new converter exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ConverterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new converter exception.
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
	public ConverterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
