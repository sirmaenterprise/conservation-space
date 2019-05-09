package com.sirma.sep.content;

/**
 * Exception thrown when content integrity checks fail.<br>
 * For example when moving content when transferred content differ from the expected content size.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/02/2019
 */
public class ContentCorruptedException extends ContentValidationException {

	/**
	 * Construct exception and specify a clarification message
	 *
	 * @param message the message describes what's when wrong
	 */
	public ContentCorruptedException(String message) {
		super(message);
	}
}
