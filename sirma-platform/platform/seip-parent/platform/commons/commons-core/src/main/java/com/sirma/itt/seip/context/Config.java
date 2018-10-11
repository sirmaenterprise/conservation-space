package com.sirma.itt.seip.context;

import java.io.Serializable;

/**
 * Represents a thread local store backed by the {@link RuntimeContext}. Can be created via
 * {@link RuntimeContext#createConfig(String, boolean)}, {@link RuntimeContext#createInheritableConfig(String)}
 */
public interface Config {

	/**
	 * Sets the given value to the thread local using the current key.
	 *
	 * @param value
	 *            the value to set
	 */
	void set(Serializable value);

	/**
	 * Retrieves a value from thread local context using the current key.
	 *
	 * @return the value or <code>null</code> if not set.
	 */
	Serializable get();

	/**
	 * Clear the set value for the current key.
	 */
	void clear();

	/**
	 * Checks if there is a value associated with the current key.
	 *
	 * @return <code>true</code>, if there is a non <code>null</code> value set.
	 */
	boolean isSet();
}
