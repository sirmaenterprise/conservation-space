package com.sirma.itt.seip.provider;

import java.util.Set;

/**
 * Base interface to realize a provider register.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
public interface ProviderRegistry<K, V> {

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	Set<K> getKeys();

	/**
	 * Find.
	 *
	 * @param key
	 *            the key
	 * @return the v
	 */
	V find(K key);

	/**
	 * Forces the register to reloads it state. This is needed if changes are made that the register content is out of
	 * date to repopulate it.
	 */
	void reload();
}
