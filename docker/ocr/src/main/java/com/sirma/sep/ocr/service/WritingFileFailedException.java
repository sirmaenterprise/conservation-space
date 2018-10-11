package com.sirma.sep.ocr.service;

/**
 * Exception thrown when the service fail to write the OCRed file.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/10/2017
 */
class WritingFileFailedException extends RuntimeException {

	/**
	 * Constructs a new exception
	 *
	 * @param message the error message.
	 */
	WritingFileFailedException(String message) {
		super(message);
	}
}
