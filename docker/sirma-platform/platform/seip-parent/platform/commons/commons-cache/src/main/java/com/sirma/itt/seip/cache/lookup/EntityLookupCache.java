package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.SimpleCache;

/**
 * A cache for two-way lookups of database entities. These are characterized by having a unique key (perhaps a database
 * ID) and a separate unique key that identifies the object. If no cache is given, then all calls are passed through to
 * the backing DAO.
 * <p>
 * The keys must have good <code>equals</code> and <code>hashCode</code> implementations and must respect the
 * case-sensitivity of the use-case.
 * <p>
 * All keys will be unique to the given cache region, allowing the cache to be shared between instances of this class.
 * <p>
 * Generics:
 * <ul>
 * <li>K: The database unique identifier.</li>
 * <li>V: The value stored against K.</li>
 * <li>VK: The a value-derived key that will be used as a cache key when caching K for lookups by V. This can be the
 * value itself if it is itself a good key.</li>
 * </ul>
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @param <S>
 *            the secondary (value) key type
 * @author Derek Hulley
 * @since 3.2
 */
public class EntityLookupCache<K extends Serializable, V, S extends Serializable>
		implements Destroyable {

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

	private final EntityLookupCallbackDAO<K, V, S> entityLookup;

	private final String cacheRegion;

	private boolean secondaryKeyEnabled = false;

	/**
	 * Construct the lookup cache <b>without any cache</b>. All calls are passed directly to the underlying DAO entity
	 * lookup.
	 *
	 * @param entityLookup
	 *            the instance that is able to find and persist entities
	 */
	public EntityLookupCache(EntityLookupCallbackDAO<K, V, S> entityLookup) {
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
			EntityLookupCallbackDAO<K, V, S> entityLookup) {
		this(cache, CACHE_REGION_DEFAULT, entityLookup);
	}

	/**
	 * Construct the lookup cache, using the given cache region.
	 * <p>
	 * All keys will be unique to the given cache region, allowing the cache to be shared between instances of this
	 * class.
	 *
	 * @param cache
	 *            the cache that will back the two-way lookups; <tt>null</tt> to have no backing in a cache.
	 * @param cacheRegion
	 *            the region within the cache to use.
	 * @param entityLookup
	 *            the instance that is able to find and persist entities
	 */
	@SuppressWarnings("unchecked")
	public EntityLookupCache(@SuppressWarnings("rawtypes") SimpleCache cache, String cacheRegion,
			EntityLookupCallbackDAO<K, V, S> entityLookup) {
		this.cache = cache;
		this.cacheRegion = cacheRegion;
		this.entityLookup = entityLookup;
		secondaryKeyEnabled = entityLookup.isSecondaryKeyEnabled();
	}

	/**
	 * Find the entity associated with the given key. The {@link EntityLookupCallbackDAO#findByKey(Serializable) entity
	 * callback} will be used if necessary.
	 * <p>
	 * It is up to the client code to decide if a <tt>null</tt> return value indicates a concurrency violation or not;
	 * the former would normally result in a concurrency-related exception.
	 *
	 * @param key
	 *            The entity key, which may be valid or invalid (<tt>null</tt> not allowed)
	 * @return Returns the key-value pair or <tt>null</tt> if the key doesn't reference an entity
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getByKey(K key) { // NOSONAR
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
		// Resolve it
		Pair<K, V> entityPair = entityLookup.findByKey(key);
		if (entityPair == null) {
			// Cache "not found"
			cache.put(keyCacheKey, VALUE_NOT_FOUND);
		} else {
			value = entityPair.getSecond();
			if (secondaryKeyEnabled) {
				// Get the value key
				S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
				// Check if the value has a good key
				if (valueKey != null) {
					CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
					// The key is good, so we can cache the value
					// BB: the callback could change the key
					cache.put(valueCacheKey, entityPair.getFirst());
				}
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
	 * Check if cache contains value. If cache is not initialized value is checked using the {@link #entityLookup}
	 * 
	 * @param key
	 *            is the key. Requires non null
	 * @return true if cache contains the provided key.
	 */
	public boolean containsKey(K key) {
		if (key == null) {
			throw new IllegalArgumentException("An entity lookup key may not be null");
		}
		// Handle missing cache
		if (cache == null) {
			return entityLookup.findByKey(key) != null;
		}
		// Look in the cache
		return cache.contains(new CacheRegionKey(cacheRegion, key));
	}

	/**
	 * Find the entity associated with the given value. The {@link EntityLookupCallbackDAO#findByValue(Object) entity
	 * callback} will be used if no entry exists in the cache.
	 * <p>
	 * It is up to the client code to decide if a <tt>null</tt> return value indicates a concurrency violation or not;
	 * the former would normally result in a concurrency-related exception.
	 *
	 * @param value
	 *            The entity value, which may be valid or invalid (<tt>null</tt> is allowed)
	 * @return Returns the key-value pair or <tt>null</tt> if the value doesn't reference an entity
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getByValue(V value) { // NOSONAR
		checkIsSecondaryKeyEnabled();
		// Handle missing cache
		if (cache == null) {
			return entityLookup.findByValue(value);
		}

		// Get the value key.
		// The cast to (VK) is counter-intuitive, but works because they're all
		// just Serializable
		// It's nasty, but hidden from the cache client code.
		S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
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

	private void checkIsSecondaryKeyEnabled() {
		if (!secondaryKeyEnabled) {
			throw new IllegalStateException("Secondary key look up is not enabled!");
		}
	}

	/**
	 * Find the entity associated with the given value and create it if it doesn't exist. The
	 * {@link EntityLookupCallbackDAO#findByValue(Object)} and {@link EntityLookupCallbackDAO#createValue(Object)} will
	 * be used if necessary.
	 *
	 * @param value
	 *            The entity value (<tt>null</tt> is allowed)
	 * @return Returns the key-value pair (new or existing and never <tt>null</tt>)
	 */
	@SuppressWarnings("unchecked")
	public Pair<K, V> getOrCreateByValue(V value) { // NOSONAR
		checkIsSecondaryKeyEnabled();
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
		S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
		// Check if the value has a good key
		if (valueKey == null) {
			Pair<K, V> entityPair = entityLookup.findByValue(value);
			if (entityPair == null) {
				entityPair = entityLookup.createValue(value);
				// Cache the value
				cache.put(new CacheRegionKey(cacheRegion, entityPair.getFirst()),
						entityPair.getSecond() == null ? VALUE_NULL : entityPair.getSecond());
			}
			return entityPair;
		}

		// Look in the cache
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if the value is already mapped to a key
		if (key != null && !key.equals(VALUE_NOT_FOUND)) {
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
	 * {@link EntityLookupCallbackDAO#updateValue(Serializable, Object)} callback will be used if necessary.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
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

		if (secondaryKeyEnabled) {
			// Get the value key.
			S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
			// Check if the value has a good key
			if (valueKey != null) {
				// There is a good value key, cache by value
				CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
				cache.put(valueCacheKey, key);
			}
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
	public K getKey(S valueKey) {
		// handle missing cache
		if (cache == null || !isSecondaryKeyEnabled()) {
			return null;
		}
		// There is a good value key, cache by value
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if we have looked this up already
		if (key != null && key.equals(VALUE_NOT_FOUND)) {
			key = null;
		}
		return key;
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

		if (isSecondaryKeyEnabled()) {
			// Get the value key.
			S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
			// Check if the value has a good key
			if (valueKey != null) {
				// There is a good value key, cache by value
				CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
				cache.put(valueCacheKey, key);
			}
		}
		// Cache by key
		cache.put(new CacheRegionKey(cacheRegion, key), value == null ? VALUE_NULL : value);
		// Done
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
		if (cache == null) {
			// no cache so we cannto fetch from there
			return null;
		}
		CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
		// Look in the cache
		V value = (V) cache.get(keyCacheKey);
		if (value == null) {
			return null;
		} else if (value.equals(VALUE_NOT_FOUND)) {
			// We checked before
			return null;
		} else if (value.equals(VALUE_NULL)) {
			return null;
		} else {
			return value;
		}
	}

	/**
	 * Delete the entity associated with the given key. The {@link EntityLookupCallbackDAO#deleteByKey(Serializable)}
	 * callback will be used if necessary.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
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
	 * Delete the entity having the given value.. The {@link EntityLookupCallbackDAO#deleteByValue(Object)} callback
	 * will be used if necessary.
	 * <p>
	 * It is up to the client code to decide if a <tt>0</tt> return value indicates a concurrency violation or not.
	 *
	 * @param value
	 *            the value
	 * @return Returns the row deletion count
	 */
	public int deleteByValue(V value) {
		if (!secondaryKeyEnabled) {
			return 0;
		}
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
		if (isSecondaryKeyEnabled() && value != null && !value.equals(VALUE_NOT_FOUND) && !value.equals(VALUE_NULL)) {
			// Get the value key and remove it
			S valueKey = entityLookup.getValueKey(value);
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
		checkIsSecondaryKeyEnabled();

		// Get the value key
		S valueKey = value == null ? (S) VALUE_NULL : entityLookup.getValueKey(value);
		if (valueKey == null) {
			// No key generated for the value. There is nothing that can be
			// done.
			return;
		}
		// Look in the cache
		CacheRegionValueKey valueCacheKey = new CacheRegionValueKey(cacheRegion, valueKey);
		K key = (K) cache.get(valueCacheKey);
		// Check if the value is already mapped to a key
		if (key != null && !key.equals(VALUE_NOT_FOUND)) {
			CacheRegionKey keyCacheKey = new CacheRegionKey(cacheRegion, key);
			cache.remove(keyCacheKey);
		}
		cache.remove(valueCacheKey);
	}

	/**
	 * Cache-only operation: Remove all cache entries
	 * <p>
	 * <b>NOTE:</b> This operation removes ALL entries for ALL cache regions.
	 */
	public void clear() {
		// Handle missing cache
		if (cache == null) {
			return;
		}
		cache.clear();
	}

	@Override
	public void destroy() {
		Destroyable.destroy(cache);
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
		try (Stream<Serializable> keys = cache.getKeys()) {
			return keys.filter(CacheRegionKey.class::isInstance).map(k -> (K) ((CacheRegionKey) k).getCacheKey()).collect(
					Collectors.toSet());
		}
	}

	/**
	 * Returns a set of all secondary keys in the cache.
	 *
	 * @return all secondary keys in the current cache
	 */
	@SuppressWarnings("unchecked")
	public Set<S> secondaryKeys() {
		// Handle missing cache
		if (cache == null || !secondaryKeyEnabled) {
			return Collections.emptySet();
		}
		try (Stream<Serializable> keys = cache.getKeys()) {
			return keys
					.filter(CacheRegionValueKey.class::isInstance)
						.map(k -> (S) ((CacheRegionValueKey) k).getCacheValueKey())
						.collect(Collectors.toSet());
		}
	}

	public boolean isSecondaryKeyEnabled() {
		return secondaryKeyEnabled;
	}

	public EntityLookupCache<K, V, S> setSecondaryKeyEnabled(boolean secondaryKeyEnabled) {
		this.secondaryKeyEnabled = secondaryKeyEnabled;
		return this;
	}

	/**
	 * Key-wrapper used to separate cache regions, allowing a single cache to be used for different purposes.<br>
	 * This class is distinct from the ID key so that ID-based lookups don't class with value-based lookups.
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
		CacheRegionKey(String cacheRegion, Serializable cacheKey) {
			this.cacheRegion = cacheRegion;
			this.cacheKey = cacheKey;
			this.hashCode = cacheRegion.hashCode() + cacheKey.hashCode();
		}

		Serializable getCacheKey() {
			return cacheKey;
		}

		@Override
		public String toString() {
			return cacheRegion + "." + cacheKey;
		}

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

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	/**
	 * Value-key-wrapper used to separate cache regions, allowing a single cache to be used for different purposes. <br>
	 * This class is distinct from the region key so that ID-based lookups don't class with value-based lookups.
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
		CacheRegionValueKey(String cacheRegion, Serializable cacheValueKey) {
			this.cacheRegion = cacheRegion;
			this.cacheValueKey = cacheValueKey;
			this.hashCode = cacheRegion.hashCode() + cacheValueKey.hashCode();
		}

		Serializable getCacheValueKey() {
			return cacheValueKey;
		}

		@Override
		public String toString() {
			return cacheRegion + "." + cacheValueKey;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof CacheRegionValueKey)) {
				return false;
			}
			CacheRegionValueKey that = (CacheRegionValueKey) obj;
			return this.cacheRegion.equals(that.cacheRegion) && this.cacheValueKey.equals(that.cacheValueKey);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
