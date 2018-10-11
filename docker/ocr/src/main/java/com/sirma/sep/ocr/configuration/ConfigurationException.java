package com.sirma.sep.ocr.configuration;

/**
 * Exception thrown when one of the mandatory configurations is missing.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
class ConfigurationException extends RuntimeException {

	/**
	 * Constructs a new exception
	 *
	 * @param message the error message.
	 */
	ConfigurationException(String message) {
		super(message);
	}

}
