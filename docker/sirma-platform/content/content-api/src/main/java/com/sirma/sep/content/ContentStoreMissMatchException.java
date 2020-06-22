package com.sirma.sep.content;

/**
 * Exception thrown to indicate wrong usage of {@link StoreItemInfo} with wrong {@link ContentStore}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/01/2018
 */
public class ContentStoreMissMatchException extends StoreException { // NOSONAR

	/**
	 * Instantiate an exception instance with the expected and the actual content stores
	 *
	 * @param expected the expected content store
	 * @param actual the actual content store name
	 */
	public ContentStoreMissMatchException(String expected, String actual) {
		super("Expected " + expected + " content store data but got " + actual);
	}
}
