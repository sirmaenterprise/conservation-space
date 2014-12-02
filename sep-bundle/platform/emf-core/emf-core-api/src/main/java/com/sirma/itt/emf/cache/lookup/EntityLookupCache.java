/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.cache.lookup;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.SimpleCache;
import com.sirma.itt.emf.domain.Pair;

/**
 * A cache for two-way lookups of database entities. These are characterized by having a unique key
 * (perhaps a database ID) and a separate unique key that identifies the object. If no cache is
 * given, then all calls are passed through to the backing DAO.
 * <p>
 * The keys must have good <code>equals</code> and </code>hashCode</code> implementations and must
 * respect the case-sensitivity of the use-case.
 * <p>
 * All keys will be unique to the given cache region, allowing the cache to be shared between
 * instances of this class.
 * <p>
 * Generics:
 * <ul>
 * <li>K: The database unique identifier.</li>
 * <li>V: The value stored against K.</li>
 * <li>VK: The a value-derived key that will be used as a cache key when caching K for lookups by V.
 * This can be the value itself if it is itself a good key.</li>
 * </ul>
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @param <VK>
 *            the generic type
 * @author Derek Hulley
 * @since 3.2
 */
public class EntityLookupCache<K extends Serializable, V extends Object, VK extends Serializable> {

	/**
	 * Interface to support lookups of the entities using keys and values.
	 * 
	 * @param <K1>
	 *            the generic type
	 * @param <V1>
	 *            the generic type
	 * @param <VK1>
	 *            the generic type
	 */
	public static interface EntityLookupCallbackDAO<K1 extends Serializable, V1 extends Object, VK1 extends Serializable> {

		/**
		 * Resolve the given value into a unique value key that can be used to find the entity's ID.
		 * A return value should be small and efficient; don't return a value if this is not
		 * possible.
		 * <p/>
		 * Implementations will often return the value itself, provided that the value is both
		 * serializable and has a good <code>equals</code> and <code>hashCode</code>.
		 * <p/>
		 * Were no adequate key can be generated for the value, then <tt>null</tt> can be returned.
		 * In this case, the {@link #findByValue(Object) findByValue} method might not even do a
		 * search and just return <tt>null</tt> itself i.e. if it is difficult to look the value up
		 * in storage then it is probably difficult to generate a cache key from it, too.. In this
		 * scenario, the cache will be purely for key-based lookups
		 * 
		 * @param value
		 *            the full value being keyed (never <tt>null</tt>)
		 * @return Returns the business key representing the entity, or <tt>null</tt> if an
		 *         economical key cannot be generated.
		 */
		VK1 getValueKey(V1 value);

		/**
		 * Find an entity for a given key.
		 * 
		 * @param key
		 *            the key (ID) used to identify the entity (never <tt>null</tt>)
		 * @return Return the entity or <tt>null</tt> if no entity is exists for the ID
		 */
		Pair<K1, V1> findByKey(K1 key);

		/**
		 * Find and entity using the given value key. The <code>equals</code> and
		 * <code>hashCode</code> methods of the value object should respect case-sensitivity in the
		 * same way that this lookup treats case-sensitivity i.e. if the <code>equals</code> method
		 * is <b>case-sensitive</b> then this method should look the entity up using a
		 * <b>case-sensitive</b> search.
		 * <p/>
		 * Since this is a cache backed by some sort of database, <tt>null</tt> values are allowed
		 * by the cache. The implementation of this method can throw an exception if <tt>null</tt>
		 * is not appropriate for the use-case.
		 * <p/>
		 * If the search is impossible or expensive, this method should just return <tt>null</tt>.
		 * This would usually be the case if the {@link #getValueKey(Object) getValueKey} method
		 * also returned <tt>null</tt> i.e. if it is difficult to look the value up in storage then
		 * it is probably difficult to generate a cache key from it, too.
		 * 
		 * @param value
		 *            the value (business object) used to identify the entity ( <tt>null</tt>
		 *            allowed).
		 * @return Return the entity or <tt>null</tt> if no entity matches the given value
		 */
		Pair<K1, V1> findByValue(V1 value);

		/**
		 * Create an entity using the given values. It is valid to assume that the entity does not
		 * exist within the current transaction at least.
		 * <p/>
		 * Since persistence mechanisms often allow <tt>null</tt> values, these can be expected
		 * here. The implementation must throw an exception if <tt>null</tt> is not allowed for the
		 * specific use-case.
		 * 
		 * @param value
		 *            the value (business object) used to identify the entity ( <tt>null</tt>
		 *            allowed).
		 * @return Return the newly-created entity ID-value pair
		 */
		Pair<K1, V1> createValue(V1 value);

		/**
		 * Update the entity identified by the given key.
		 * <p/>
		 * It is up to the client code to decide if a <tt>0</tt> return value indicates a
		 * concurrency violation or not.
		 * 
		 * @param key
		 *            the existing key (ID) used to identify the entity (never <tt>null</tt>)
		 * @param value
		 *            the new value
		 * @return Returns the row update count.
		 */
		int updateValue(K1 key, V1 value);

		/**
		 * Delete an entity for the given key.
		 * <p/>
		 * It is up to the client code to decide if a <tt>0</tt> return value indicates a
		 * concurrency violation or not.
		 * 
		 * @param key
		 *            the key (ID) used to identify the entity (never <tt>null</tt>)
		 * @return Returns the row deletion count.
		 */
		int deleteByKey(K1 key);

		/**
		 * Delete an entity for the given value.
		 * <p/>
		 * It is up to the client code to decide if a <tt>0</tt> return value indicates a
		 * concurrency violation or not.
		 * 
		 * @param value
		 *            the value (business object) used to identify the enitity ( <tt>null</tt>
		 *            allowed)
		 * @return Returns the row deletion count.
		 */
		int deleteByValue(V1 value);
	}

	/**
	 * Adaptor for implementations that support immutable entities. The update and delete operations
	 * throw {@link UnsupportedOperationException}.
	 * 
	 * @param <K2>
	 *            the generic type
	 * @param <V2>
	 *            the generic type
	 * @param <VK2>
	 *            the generic type
	 * @author Derek Hulley
	 * @since 3.2
	 */
	public static abstract class EntityLookupCallbackDAOAdaptor<K2 extends Serializable, V2 extends Object, VK2 extends Serializable>
			implements EntityLookupCallbackDAO<K2, V2, VK2> {

		/**
		 * This implementation never finds a value and is backed by.
		 * 
		 * @param value
		 *            the value
		 * @return Returns <tt>null</tt> always {@link #getValueKey(Object)} returning nothing.
		 */
		@Override
		public Pair<K2, V2> findByValue(V2 value) {
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
		public VK2 getValueKey(V2 value) {
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
		public int updateValue(K2 key, V2 value) {
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
		public int deleteByKey(K2 key) {
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
		public int deleteByValue(V2 value) {
			throw new UnsupportedOperationException("Entity deletion by value is not supported");
		}
	}

	/**
	 * A valid <code>null</code> value i.e. a value that has been <u>persisted</u> as null.
	 */
	private static final Serializable VALUE_NULL = "@@VALUE_NULL@@";
	/**
	 * A value that was not found or persisted.
	 */
	private static final Serializable VALUE_NOT_FOUND = "@@VALUE_NOT_FOUND@@";

	/**
	 * The cache region that will be used (see {@link CacheRegionKey}) in all the cache keys.
	 */
	private static final String CACHE_REGION_DEFAULT = "DEFAULT";

	private final SimpleCache<Serializable, Object> cache;

	private final EntityLookupCallbackDAO<K, V, VK> entityLookup;

	private final String cacheRegion;

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityLookupCache.class);

	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/**
	 * Construct the lookup cache <b>without any cache</b>. All calls are passed directly to the
	 * underlying DAO entity lookup.
	 * 
	 * @param entityLookup
	 *            the instance that is able to find and persist entities
	 */
	public EntityLookupCache(EntityLookupCallbackDAO<K, V, VK> entityLookup) {
		this(null, CACHE_REGION_DEFAULT, entityLookup);
	}

	/**
	 * Construct the lookup cache, using the {@link #CACHE_REGION_DEFAULT default cache region}.
	 * 
	 * @param cache
	 *            the cache that will back the two-way lookups
	 * @param entityLookup
	 *            the instance that is able to find and persist entities
	 */
	public EntityLookupCache(@SuppressWarnings("rawtypes") SimpleCache cache,
			EntityLookupCallbackDAO<K, V, VK> entityLookup) {
		this(cache, CACHE_REGION_DEFAULT, entityLookup);
	}

	/**
	 * Construct the lookup cache, using the given cache region.
	 * <p>
	 * All keys will be unique to the given cache region, allowing the cache to be shared between
	 * instances of this class.
	 * 
	 * @param cache
	 *            the cache that will back the two-way lookups; <tt>null</tt> to have no backing in
	 *            a cache.
	 * @param cacheRegion
	 *            the region within the cache to use.
	 * @param entityLookup
	 *            the instance that is able to find and persist entities
	 */
	@SuppressWarnings("unchecked")
	public EntityLookupCache(@SuppressWarnings("rawtypes") SimpleCache cache, String cacheRegion,
			EntityLookupCallbackDAO<K, V, VK> entityLookup) {
		this.cache = cache;
		this.cacheRegion = cacheRegion;
		this.entityLookup = entityLookup;
	}

	/**
	 * Find the entity associated with the given key. The
	 * {@link EntityLookupCallbackDAO#findByKey(Serializable) entity callback} will be used if
	 * necessary.
	 * <p/>
	 * It is up to the client code to decide if a <tt>null</tt> return value indicates a concurrency
	 * violation or not; the former would normally result in a concurrency-related exception such as
	 * {@link ConcurrencyFailureException}.
	 * 
	 * @param key
	 *            The entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @return Returns the key-value pair or <tt>null</tt> if the key doesn't reference an entity
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getByKey(K key) {
		if (key == null) {
			throw new IllegalArgumentException("An entity lookup key may not be null");
		}
		// Handle missing cache
		if (cache == null) {
			return entityLookup.findByKey(key);
		}

		CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
		// Look in the cache
		V value = (V) cache.get(keyCacheKey);
		if (value != null) {
			if (value.equals(VALUE_NOT_FOUND)) {
				// We checked before
				return null;
			} else if (value.equals(VALUE_NULL)) {
				return new Pair<>(key, null);
			} else {
				return new Pair<>(key, value);
			}
		}
		if (TRACE) {
			LOGGER.trace("Looking up a key='" + key + "' from " + cacheRegion
					+ " region via callback");
		}
		// Resolve it
		Pair<K, V> entityPair = entityLookup.findByKey(key);
		if (entityPair == null) {
			// Cache "not found"
			cache.put(keyCacheKey, VALUE_NOT_FOUND);
		} else {
			value = entityPair.getSecond();
			// Get the value key
			VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
			// Check if the value has a good key
			if (valueKey != null) {
				CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
				// The key is good, so we can cache the value
				// BB: the callback could change the key
				cache.put(valueCacheKey, entityPair.getFirst());
			}
			// update the cache key if the callback update the key
			if (!key.equals(entityPair.getFirst())) {
				keyCacheKey = new CacheRegionKey(cacheRegion, entityPair.getFirst());
			}
			cache.put(keyCacheKey, value == null ? VALUE_NULL : value);
		}
		// Done
		return entityPair;
	}

	/**
	 * Find the entity associated with the given value. The
	 * {@link EntityLookupCallbackDAO#findByValue(Object) entity callback} will be used if no entry
	 * exists in the cache.
	 * <p/>
	 * It is up to the client code to decide if a <tt>null</tt> return value indicates a concurrency
	 * violation or not; the former would normally result in a concurrency-related exception such as
	 * {@link ConcurrencyFailureException}.
	 * 
	 * @param value
	 *            The entity value, which may be valid or invalid (<tt>null</tt> is allowed)
	 * @return Returns the key-value pair or <tt>null</tt> if the value doesn't reference an entity
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getByValue(V value) {
		// Handle missing cache
		if (cache == null) {
			return entityLookup.findByValue(value);
		}

		// Get the value key.
		// The cast to (VK) is counter-intuitive, but works because they're all
		// just Serializable
		// It's nasty, but hidden from the cache client code.
		VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
		// Check if the value has a good key
		if (valueKey == null) {
			return entityLookup.findByValue(value);
		}

		// Look in the cache
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if we have looked this up already
		if (key != null) {
			// We checked before and ...
			if (key.equals(VALUE_NOT_FOUND)) {
				// ... it didn't exist
				return null;
			}
			// ... it did exist
			return getByKey(key);
		}
		if (TRACE) {
			LOGGER.trace("Looking up a value key='" + valueKey + "' from " + cacheRegion
					+ " region via callback");
		}
		// Resolve it
		Pair<K, V> entityPair = entityLookup.findByValue(value);
		if (entityPair == null) {
			// Cache "not found"
			cache.put(valueCacheKey, VALUE_NOT_FOUND);
		} else {
			key = entityPair.getFirst();
			// Cache the key
			cache.put(valueCacheKey, key);
			cache.put(new CacheRegionKey(cacheRegion, key),
					entityPair.getSecond() == null ? VALUE_NULL : entityPair.getSecond());
		}
		// Done
		return entityPair;
	}

	/**
	 * Find the entity associated with the given value and create it if it doesn't exist. The
	 * {@link EntityLookupCallbackDAO#findByValue(Object)} and
	 * {@link EntityLookupCallbackDAO#createValue(Object)} will be used if necessary.
	 * 
	 * @param value
	 *            The entity value (<tt>null</tt> is allowed)
	 * @return Returns the key-value pair (new or existing and never <tt>null</tt>)
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getOrCreateByValue(V value) {
		// Handle missing cache
		if (cache == null) {
			Pair<K, V> entityPair = entityLookup.findByValue(value);
			if (entityPair == null) {
				entityPair = entityLookup.createValue(value);
			}
			return entityPair;
		}

		// Get the value key
		// The cast to (VK) is counter-intuitive, but works because they're all
		// just Serializable.
		// It's nasty, but hidden from the cache client code.
		VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
		// Check if the value has a good key
		if (valueKey == null) {
			Pair<K, V> entityPair = entityLookup.findByValue(value);
			if (entityPair == null) {
				entityPair = entityLookup.createValue(value);
				// Cache the value
				cache.put(new CacheRegionKey(cacheRegion, entityPair.getFirst()),
						(entityPair.getSecond() == null ? VALUE_NULL : entityPair.getSecond()));
			}
			return entityPair;
		}

		// Look in the cache
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if the value is already mapped to a key
		if ((key != null) && !key.equals(VALUE_NOT_FOUND)) {
			return new Pair<>(key, value);
		}
		// Resolve it
		Pair<K, V> entityPair = entityLookup.findByValue(value);
		if (entityPair == null) {
			// Create it
			entityPair = entityLookup.createValue(value);
		}
		key = entityPair.getFirst();
		// Cache the key and value
		cache.put(valueCacheKey, key);
		cache.put(new CacheRegionKey(cacheRegion, key), value == null ? VALUE_NULL : value);
		// Done
		return entityPair;
	}

	/**
	 * Update the entity associated with the given key. The
	 * {@link EntityLookupCallbackDAO#updateValue(Serializable, Object)} callback will be used if
	 * necessary.
	 * <p/>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency
	 * violation or not; usually the former will generate {@link ConcurrencyFailureException} or
	 * something recognized by the {@link RetryingTransactionHelper#RETRY_EXCEPTIONS
	 * RetryingTransactionHelper}.
	 * 
	 * @param key
	 *            The entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @param value
	 *            The new entity value (may be <tt>null</tt>)
	 * @return Returns the row update count.
	 */
	@SuppressWarnings("unchecked")
	public int updateValue(K key, V value) {
		// Handle missing cache
		if (cache == null) {
			return entityLookup.updateValue(key, value);
		}

		// Remove entries for the key (bidirectional removal removes the old
		// value as well)
		removeByKey(key);

		// Do the update
		int updateCount = entityLookup.updateValue(key, value);
		if (updateCount == 0) {
			// Nothing was done
			return updateCount;
		}

		// Get the value key.
		VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
		// Check if the value has a good key
		if (valueKey != null) {
			// There is a good value key, cache by value
			CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
			cache.put(valueCacheKey, key);
		}
		// Cache by key
		cache.put(new CacheRegionKey(cacheRegion, key), value == null ? VALUE_NULL : value);
		// Done
		return updateCount;
	}

	/**
	 * Cache-only operation: Get the key for a given value key (note: not 'value' but 'value key').
	 * 
	 * @param valueKey
	 *            the value key
	 * @return The entity key (may be <tt>null</tt>)
	 */
	@SuppressWarnings("unchecked")
	public K getKey(VK valueKey) {
		// There is a good value key, cache by value
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if we have looked this up already
		if ((key != null) && key.equals(VALUE_NOT_FOUND)) {
			if (TRACE) {
				LOGGER.trace(cacheRegion + ": VALUE_NOT_FOUND for VK" + valueCacheKey);
			}
			key = null;
		}
		return key;
	}

	/**
	 * Cache-only operation: Get the value for a given key.
	 * 
	 * @param key
	 *            The entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @return The entity value (may be <tt>null</tt>)
	 */
	@SuppressWarnings("unchecked")
	public V getValue(K key) {
		CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
		// Look in the cache
		V value = (V) cache.get(keyCacheKey);
		if (value == null) {
			return null;
		} else if (value.equals(VALUE_NOT_FOUND)) {
			// We checked before
			if (TRACE) {
				LOGGER.trace(cacheRegion + ": VALUE_NOT_FOUND for K" + key);
			}
			return null;
		} else if (value.equals(VALUE_NULL)) {
			return null;
		} else {
			return value;
		}
	}

	/**
	 * Cache-only operation: Update the cache's value.
	 * 
	 * @param key
	 *            The entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @param value
	 *            The new entity value (may be <tt>null</tt>)
	 */
	@SuppressWarnings("unchecked")
	public void setValue(K key, V value) {
		// Handle missing cache
		if (cache == null) {
			return;
		}

		// Remove entries for the key (bidirectional removal removes the old
		// value as well)
		removeByKey(key);

		// Get the value key.
		VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
		// Check if the value has a good key
		if (valueKey != null) {
			// There is a good value key, cache by value
			CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
			cache.put(valueCacheKey, key);
		}
		// Cache by key
		cache.put(new CacheRegionKey(cacheRegion, key), value == null ? VALUE_NULL : value);
		// Done
	}

	/**
	 * Delete the entity associated with the given key. The
	 * {@link EntityLookupCallbackDAO#deleteByKey(Serializable)} callback will be used if necessary.
	 * <p/>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency
	 * violation or not; usually the former will generate {@link ConcurrencyFailureException} or
	 * something recognized by the {@link RetryingTransactionHelper#RETRY_EXCEPTIONS
	 * RetryingTransactionHelper}.
	 * 
	 * @param key
	 *            the entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @return Returns the row deletion count
	 */
	public int deleteByKey(K key) {
		// Handle missing cache
		if (cache == null) {
			return entityLookup.deleteByKey(key);
		}

		// Remove entries for the key (bidirectional removal removes the old
		// value as well)
		removeByKey(key);

		// Do the delete
		return entityLookup.deleteByKey(key);
	}

	/**
	 * Delete the entity having the given value.. The
	 * {@link EntityLookupCallbackDAO#deleteByValue(Object)} callback will be used if necessary.
	 * <p/>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency
	 * violation or not; usually the former will generate {@link ConcurrencyFailureException} or
	 * something recognized by the {@link RetryingTransactionHelper#RETRY_EXCEPTIONS
	 * RetryingTransactionHelper}.
	 * 
	 * @param value
	 *            the value
	 * @return Returns the row deletion count
	 */
	public int deleteByValue(V value) {
		// Handle missing cache
		if (cache == null) {
			return entityLookup.deleteByValue(value);
		}

		// Remove entries for the value
		removeByValue(value);

		// Do the delete
		return entityLookup.deleteByValue(value);
	}

	/**
	 * Cache-only operation: Remove all cache values associated with the given key.
	 * 
	 * @param key
	 *            the key
	 */
	@SuppressWarnings("unchecked")
	public void removeByKey(K key) {
		// Handle missing cache
		if (cache == null) {
			return;
		}

		CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
		V value = (V) cache.get(keyCacheKey);
		if ((value != null) && !value.equals(VALUE_NOT_FOUND) && !value.equals(VALUE_NULL)) {
			// Get the value key and remove it
			VK valueKey = entityLookup.getValueKey(value);
			if (valueKey != null) {
				CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
				cache.remove(valueCacheKey);
			}
		}
		cache.remove(keyCacheKey);
	}

	/**
	 * Cache-only operation: Remove all cache values associated with the given value.
	 * 
	 * @param value
	 *            The entity value (<tt>null</tt> is allowed)
	 */
	@SuppressWarnings("unchecked")
	public void removeByValue(V value) {
		// Handle missing cache
		if (cache == null) {
			return;
		}

		// Get the value key
		VK valueKey = (value == null) ? (VK) VALUE_NULL : entityLookup.getValueKey(value);
		if (valueKey == null) {
			// No key generated for the value. There is nothing that can be
			// done.
			return;
		}
		// Look in the cache
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if the value is already mapped to a key
		if ((key != null) && !key.equals(VALUE_NOT_FOUND)) {
			CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
			cache.remove(keyCacheKey);
		}
		cache.remove(valueCacheKey);
	}

	/**
	 * Cache-only operation: Remove all cache entries
	 * <p/>
	 * <b>NOTE:</b> This operation removes ALL entries for ALL cache regions.
	 */
	public void clear() {
		// Handle missing cache
		if (cache == null) {
			return;
		}
		cache.clear();
	}

	/**
	 * Returns a set of all primary keys in the cache.
	 * 
	 * @return all primary keys in the current cache
	 */
	@SuppressWarnings("unchecked")
	public Set<K> primaryKeys() {
		// Handle missing cache
		if (cache == null) {
			return Collections.emptySet();
		}
		Collection<Serializable> keys = cache.getKeys();
		Set<K> result = new HashSet<>((int) (keys.size() * 1.1), 0.95f);
		for (Serializable serializable : keys) {
			if (serializable instanceof CacheRegionKey) {
				result.add((K) ((CacheRegionKey) serializable).cacheKey);
			}
		}
		return result;
	}

	/**
	 * Returns a set of all secondary keys in the cache.
	 * 
	 * @return all secondary keys in the current cache
	 */
	@SuppressWarnings("unchecked")
	public Set<VK> secondaryKeys() {
		// Handle missing cache
		if (cache == null) {
			return Collections.emptySet();
		}
		Collection<Serializable> keys = cache.getKeys();
		Set<VK> result = new HashSet<>((int) (keys.size() * 1.1), 0.95f);
		for (Serializable serializable : keys) {
			if (serializable instanceof CacheRegionValueKey) {
				result.add((VK) ((CacheRegionValueKey) serializable).cacheValueKey);
			}
		}
		return result;
	}

	/**
	 * Key-wrapper used to separate cache regions, allowing a single cache to be used for different
	 * purposes.<b/> This class is distinct from the ID key so that ID-based lookups don't class
	 * with value-based lookups.
	 */
	private static class CacheRegionKey implements Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -213050301938804468L;

		/** The cache region. */
		private final String cacheRegion;

		/** The cache key. */
		private final Serializable cacheKey;

		/** The hash code. */
		private final int hashCode;

		/**
		 * Instantiates a new cache region key.
		 * 
		 * @param cacheRegion
		 *            the cache region
		 * @param cacheKey
		 *            the cache key
		 */
		private CacheRegionKey(String cacheRegion, Serializable cacheKey) {
			this.cacheRegion = cacheRegion;
			this.cacheKey = cacheKey;
			this.hashCode = cacheRegion.hashCode() + cacheKey.hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return new StringBuilder(cacheRegion).append(".").append(cacheKey).toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof CacheRegionKey)) {
				return false;
			}
			CacheRegionKey that = (CacheRegionKey) obj;
			return this.cacheRegion.equals(that.cacheRegion) && this.cacheKey.equals(that.cacheKey);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	/**
	 * Value-key-wrapper used to separate cache regions, allowing a single cache to be used for
	 * different purposes.<b/> This class is distinct from the region key so that ID-based lookups
	 * don't class with value-based lookups.
	 */
	private static class CacheRegionValueKey implements Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5838308035326617927L;

		/** The cache region. */
		private final String cacheRegion;

		/** The cache value key. */
		private final Serializable cacheValueKey;

		/** The hash code. */
		private final int hashCode;

		/**
		 * Instantiates a new cache region value key.
		 * 
		 * @param cacheRegion
		 *            the cache region
		 * @param cacheValueKey
		 *            the cache value key
		 */
		private CacheRegionValueKey(String cacheRegion, Serializable cacheValueKey) {
			this.cacheRegion = cacheRegion;
			this.cacheValueKey = cacheValueKey;
			this.hashCode = cacheRegion.hashCode() + cacheValueKey.hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return new StringBuilder().append(cacheRegion).append(".").append(cacheValueKey)
					.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof CacheRegionValueKey)) {
				return false;
			}
			CacheRegionValueKey that = (CacheRegionValueKey) obj;
			return this.cacheRegion.equals(that.cacheRegion)
					&& this.cacheValueKey.equals(that.cacheValueKey);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
