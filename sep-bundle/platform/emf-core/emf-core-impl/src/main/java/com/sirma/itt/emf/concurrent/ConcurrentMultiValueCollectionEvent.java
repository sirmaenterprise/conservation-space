package com.sirma.itt.emf.concurrent;

import java.io.Serializable;

/**
 * Event used with the {@link ConcurrentMultiValueCollection}.
 * 
 * @author nvelkov
 */
public class ConcurrentMultiValueCollectionEvent {

	/** The key. */
	private Serializable key;

	/** The value. */
	private Serializable value;

	/**
	 * Instantiates a new event.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public ConcurrentMultiValueCollectionEvent(Serializable key, Serializable value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public Serializable getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            the new key
	 */
	public void setKey(Serializable key) {
		this.key = key;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(Serializable value) {
		this.value = value;
	}

}
