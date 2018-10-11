package com.sirma.itt.seip.resources;

/**
 * Thrown when mutable store method is called for read only store.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/08/2017
 */
public class ReadOnlyStoreException extends RemoteStoreException {

	/**
	 * Instantiate with a default message.
	 */
	public ReadOnlyStoreException() {
		this("Operation not allowed for read only remote store");
	}

	/**
	 * Instantiate new instance with the given message
	 *
	 * @param message the message to set
	 */
	public ReadOnlyStoreException(String message) {
		super(message);
	}
}
