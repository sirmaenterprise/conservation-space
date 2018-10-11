package com.sirma.sep.ocr.entity;

/**
 * Exception thrown in case output jms message cannot be built.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/10/2017
 */
class MessageBuildException extends RuntimeException {

	/**
	 * Constructs a new exception.
	 *
	 * @param message the error message.
	 */
	MessageBuildException(String message) {
		super(message);
	}
}
