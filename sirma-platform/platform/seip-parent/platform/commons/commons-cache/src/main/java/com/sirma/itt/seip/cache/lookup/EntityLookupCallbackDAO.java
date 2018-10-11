package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;

/**
 * Interface to support lookups of the entities using keys and values.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @param <S>
 *            the secondary key type
 */
public interface EntityLookupCallbackDAO<K extends Serializable, V extends Object, S extends Serializable> {

	/**
	 * Resolve the given value into a unique value key that can be used to find the entity's ID. A return value should
	 * be small and efficient; don't return a value if this is not possible.
	 * <p>
	 * Implementations will often return the value itself, provided that the value is both serializable and has a good
	 * <code>equals</code> and <code>hashCode</code>.
	 * <p>
	 * Were no adequate key can be generated for the value, then <tt>null</tt> can be returned. In this case, the
	 * {@link #findByValue(Object) findByValue} method might not even do a search and just return <tt>null</tt> itself
	 * i.e. if it is difficult to look the value up in storage then it is probably difficult to generate a cache key
	 * from it, too.. In this scenario, the cache will be purely for key-based lookups
	 *
	 * @param value
	 *            the full value being keyed (never <tt>null</tt>)
	 * @return Returns the business key representing the entity, or <tt>null</tt> if an economical key cannot be
	 *         generated.
	 */
	S getValueKey(V value);

	/**
	 * Find an entity for a given key.
	 *
	 * @param key
	 *            the key (ID) used to identify the entity (never <tt>null</tt>)
	 * @return Return the entity or <tt>null</tt> if no entity is exists for the ID
	 */
	Pair<K, V> findByKey(K key);

	/**
	 * Find and entity using the given value key. The <code>equals</code> and <code>hashCode</code> methods of the value
	 * object should respect case-sensitivity in the same way that this lookup treats case-sensitivity i.e. if the
	 * <code>equals</code> method is <b>case-sensitive</b> then this method should look the entity up using a
	 * <b>case-sensitive</b> search.
	 * <p>
	 * Since this is a cache backed by some sort of database, <tt>null</tt> values are allowed by the cache. The
	 * implementation of this method can throw an exception if <tt>null</tt> is not appropriate for the use-case.
	 * <p>
	 * If the search is impossible or expensive, this method should just return <tt>null</tt>. This would usually be the
	 * case if the {@link #getValueKey(Object) getValueKey} method also returned <tt>null</tt> i.e. if it is difficult
	 * to look the value up in storage then it is probably difficult to generate a cache key from it, too.
	 *
	 * @param value
	 *            the value (business object) used to identify the entity ( <tt>null</tt> allowed).
	 * @return Return the entity or <tt>null</tt> if no entity matches the given value
	 */
	Pair<K, V> findByValue(V value);

	/**
	 * Create an entity using the given values. It is valid to assume that the entity does not exist within the current
	 * transaction at least.
	 * <p>
	 * Since persistence mechanisms often allow <tt>null</tt> values, these can be expected here. The implementation
	 * must throw an exception if <tt>null</tt> is not allowed for the specific use-case.
	 *
	 * @param value
	 *            the value (business object) used to identify the entity ( <tt>null</tt> allowed).
	 * @return Return the newly-created entity ID-value pair
	 */
	Pair<K, V> createValue(V value);

	/**
	 * Update the entity identified by the given key.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
	 *
	 * @param key
	 *            the existing key (ID) used to identify the entity (never <tt>null</tt>)
	 * @param value
	 *            the new value
	 * @return Returns the row update count.
	 */
	int updateValue(K key, V value);

	/**
	 * Delete an entity for the given key.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
	 *
	 * @param key
	 *            the key (ID) used to identify the entity (never <tt>null</tt>)
	 * @return Returns the row deletion count.
	 */
	int deleteByKey(K key);

	/**
	 * Delete an entity for the given value.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
	 *
	 * @param value
	 *            the value (business object) used to identify the enitity ( <tt>null</tt> allowed)
	 * @return Returns the row deletion count.
	 */
	int deleteByValue(V value);
}