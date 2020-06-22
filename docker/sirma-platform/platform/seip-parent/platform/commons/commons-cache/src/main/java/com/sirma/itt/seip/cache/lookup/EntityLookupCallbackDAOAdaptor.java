package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;

/**
 * Adaptor for implementations that support immutable entities. The update and delete operations throw
 * {@link UnsupportedOperationException}.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @param <S>
 *            the secondary key type
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class EntityLookupCallbackDAOAdaptor<K extends Serializable, V, S extends Serializable>
		implements EntityLookupCallbackDAO<K, V, S> {

	protected boolean secondaryKeyEnabled = false;

	/**
	 * This implementation never finds a value and is backed by.
	 *
	 * @param value
	 *            the value
	 * @return Returns <tt>null</tt> always {@link #getValueKey(Object)} returning nothing.
	 */
	@Override
	public Pair<K, V> findByValue(V value) {
		return null;
	}

	/**
	 * This implementation does not find by value and is backed by.
	 *
	 * @param value
	 *            the value
	 * @return Returns <tt>null</tt> always {@link #findByValue(Object)} returning nothing.
	 */
	@Override
	public S getValueKey(V value) {
		return null;
	}

	/**
	 * Disallows the operation.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the int
	 */
	@Override
	public int updateValue(K key, V value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Disallows the operation.
	 *
	 * @param key
	 *            the key
	 * @return the int
	 */
	@Override
	public int deleteByKey(K key) {
		throw new UnsupportedOperationException("Entity deletion by key is not supported");
	}

	/**
	 * Disallows the operation.
	 *
	 * @param value
	 *            the value
	 * @return the int
	 */
	@Override
	public int deleteByValue(V value) {
		throw new UnsupportedOperationException("Entity deletion by value is not supported");
	}

	/**
	 * Enables secondary key management
	 *
	 * @return the current instance for chaining
	 */
	@Override
	public EntityLookupCallbackDAO<K, V, S> enableSecondaryKeyManagement() {
		secondaryKeyEnabled = true;
		return this;
	}

	@Override
	public boolean isSecondaryKeyEnabled() {
		return secondaryKeyEnabled;
	}
}