package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.model.Instance;

// TODO: Auto-generated Javadoc
/**
 * Collections utils class similar to {@link java.util.Collections}. The provided empty collections
 * here are not immutable and are suitable for web use.
 * 
 * @author BBonev
 */
public class CollectionUtils {

	/** The Constant EMPTY_MAP. */
	@SuppressWarnings("rawtypes")
	public static final Map EMPTY_MAP = new HashMap<Object, Object>(0);

	/** The Constant EMPTY_SET. */
	@SuppressWarnings("rawtypes")
	public static final Set EMPTY_SET = new HashSet<Object>(0);

	/** The Constant EMPTY_LIST. */
	@SuppressWarnings("rawtypes")
	public static final List EMPTY_LIST = new ArrayList<Object>(0);

	/**
	 * Gets the empty map.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the empty map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> emptyMap() {
		// ensure emptiness
		EMPTY_MAP.clear();
		return EMPTY_MAP;
	}

	/**
	 * Empty set.
	 * 
	 * @param <E>
	 *            the element type
	 * @return the sets the
	 */
	@SuppressWarnings("unchecked")
	public static <E> Set<E> emptySet() {
		// ensure emptiness
		EMPTY_SET.clear();
		return EMPTY_SET;
	}

	/**
	 * Empty list.
	 * 
	 * @param <E>
	 *            the element type
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> emptyList() {
		// ensure emptiness
		EMPTY_LIST.clear();
		return EMPTY_LIST;
	}

	/**
	 * Adds the value to map. If the key does not exists new value list is created and the value is
	 * added to it
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param newValue
	 *            the new value
	 */
	public static <K, V> void addValueToMap(Map<K, List<V>> map, K key, V newValue) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new LinkedList<V>();
			map.put(key, list);
		}
		list.add(newValue);
	}

	/**
	 * Adds the value to map. If the key does not exists new value set is created and the value is
	 * added to it
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param newValue
	 *            the new value
	 */
	public static <K, V> void addValueToSetMap(Map<K, Set<V>> map, K key, V newValue) {
		Set<V> list = map.get(key);
		if (list == null) {
			list = new LinkedHashSet<V>();
			map.put(key, list);
		}
		list.add(newValue);
	}

	/**
	 * Creates the linked hash map instance that will hold the given amount of elements. The
	 * returned instance is optimized for that size and will not grow until the given number of
	 * elements are added.
	 * <p>
	 * The method is same as <code>new LinkedHashMap<K, V>((int) (size * 1.1), 0.95f)</code>
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <K, V> Map<K, V> createLinkedHashMap(int size) {
		return new LinkedHashMap<K, V>((int) (size * 1.1), 0.95f);
	}

	/**
	 * Creates the hash map instance that will hold the given amount of elements. The returned
	 * instance is optimized for that size and will not grow until the given number of elements are
	 * added.
	 * <p>
	 * The method is same as <code>new HashMap<K, V>((int) (size * 1.1), 0.95f)</code>
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <K, V> Map<K, V> createHashMap(int size) {
		return new HashMap<K, V>((int) (size * 1.1), 0.95f);
	}

	/**
	 * Creates the hash set instance that will hold the given amount of elements. The returned
	 * instance is optimized for that size and will not grow until the given number of elements are
	 * added.
	 * <p>
	 * The method is same as <code>new HashSet<V>((int) (size * 1.1), 0.95f)</code>
	 * 
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <V> Set<V> createHashSet(int size) {
		return new HashSet<V>((int) (size * 1.1), 0.95f);
	}

	/**
	 * Creates the linked hash set instance that will hold the given amount of elements. The
	 * returned instance is optimized for that size and will not grow until the given number of
	 * elements are added.
	 * <p>
	 * The method is same as <code>new LinkedHashSet<V>((int) (size * 1.1), 0.95f)</code>
	 * 
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <V> Set<V> createLinkedHashSet(int size) {
		return new LinkedHashSet<V>((int) (size * 1.1), 0.95f);
	}

	/**
	 * Searches for an element in a list from some index ahead. If not found -1 is returned
	 * 
	 * @param <V>
	 *            the list element type
	 * @param list
	 *            the source list
	 * @param value
	 *            the value to search for
	 * @param fromIndex
	 *            the from index
	 * @return the index position of the found element or -1 if not found
	 */
	public static <V> int indexOf(List<V> list, V value, int fromIndex) {
		if ((list == null) || list.isEmpty() || (list.size() <= fromIndex)) {
			return -1;
		}
		for (int i = fromIndex; i < list.size(); i++) {
			V v = list.get(i);
			if (EqualsHelper.nullSafeEquals(v, value)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Copy value.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param key
	 *            the key
	 */
	public static <K, V> void copyValue(Map<K, V> source, Map<K, V> target, K key) {
		target.put(key, source.get(key));
	}

	/**
	 * Copy value from the source map to the target map only if the value exists in the source map.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source map
	 * @param target
	 *            the target map
	 * @param key
	 *            the key to copy
	 * @return <code>true</code> if exists and copied
	 */
	public static <K, V> boolean copyValueIfExist(Map<K, V> source, Map<K, V> target, K key) {
		V v = source.get(key);
		if (v != null) {
			target.put(key, v);
			return true;
		}
		return false;
	}

	/**
	 * Copy value.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 * @param target
	 *            the target
	 * @param targetKey
	 *            the target key
	 */
	public static <K, V> void copyValue(Map<K, V> source, K sourceKey, Map<K, V> target, K targetKey) {
		target.put(targetKey, source.get(sourceKey));
	}

	/**
	 * Copy value.
	 * 
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 * @param target
	 *            the target
	 * @param targetKey
	 *            the target key
	 */
	public static void copyValue(Instance source, String sourceKey, Instance target, String targetKey) {
		target.getProperties().put(targetKey, source.getProperties().get(sourceKey));
	}

	/**
	 * Copy values.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param keys
	 *            the keys
	 */
	public static <K, V> void copyValues(Map<K, V> source, Map<K, V> target, Set<K> keys) {
		for (K key : keys) {
			target.put(key, source.get(key));
		}
	}

	/**
	 * Copy values from the source map to the target map if the value exists in the source map.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param keys
	 *            the keys to copy
	 */
	public static <K, V> void copyValuesIfExist(Map<K, V> source, Map<K, V> target, Set<K> keys) {
		for (K key : keys) {
			copyValueIfExist(source, target, key);
		}
	}

	/**
	 * Checks if the given collection of strings contains the provided string ignoring case.
	 * <p>
	 * <b>NOTE:</b> for optimal results if possible construct your collection only with lower/upper
	 * case strings. It's going to be faster if the checks need to be performed often.
	 * 
	 * @param collection
	 *            the collection to check if contains the given string
	 * @param value
	 *            the value to check for
	 * @return <code>true</code>, if found into the collection ignoring case.
	 */
	public static boolean containsIgnoreCase(Collection<String> collection, String value) {
		if (value == null) {
			return false;
		}
		for (String string : collection) {
			if (string.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds elements to a collection. The method modifies the supplied collection by adding the
	 * elements from the variable arguments parameter if any.
	 * 
	 * @param <C>
	 *            the collection type
	 * @param <E>
	 *            the elements type
	 * @param collection
	 *            the collection to add the elements to. The collection implementation should
	 *            support <code>null</code> values if such are expected in the second argument.
	 * @param elements
	 *            a list of elements to add to the specified collection. If any of the elements is
	 *            <code>null</code> it will be added to the collection.
	 * @return the same collection instance as the argument with added elements
	 */
	@SafeVarargs
	public static <C extends Collection<E>, E> C addToCollection(C collection, E... elements) {
		if ((collection == null) || (elements == null) || (elements.length == 0)) {
			return collection;
		}
		for (E e : elements) {
			collection.add(e);
		}
		return collection;
	}

	/**
	 * Adds the given {@link Pair} arguments to the given map. If the map is <code>null</code> new
	 * one will be created for the given arguments. If both arguments are <code>null</code>
	 * {@link Collections#EMPTY_MAP} will be returned.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map to populate if present or <code>null</code> to create new one using the
	 *            method {@link #createLinkedHashMap(int)}
	 * @param pairs
	 *            the pairs to add to the map. The first element of the pair will be the key and the
	 *            second value.
	 * @return the updated map instance or new map with the added pair elements.
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> addToMap(Map<K, V> map, Pair<K, V>... pairs) {
		Map<K, V> local = map;
		if (local == null) {
			if (pairs != null) {
				local = createLinkedHashMap(pairs.length);
			} else {
				return Collections.emptyMap();
			}
		}
		if (pairs != null) {
			for (int i = 0; i < pairs.length; i++) {
				Pair<K, V> pair = pairs[i];
				local.put(pair.getFirst(), pair.getSecond());
			}
		}
		return local;
	}

	/**
	 * Converts the given collection to entity mapping where the key is the entity id of the given
	 * instance and the value is the instance itself.
	 * 
	 * @param <S>
	 *            the entity id type
	 * @param <I>
	 *            the entity type
	 * @param collection
	 *            the collection
	 * @return the map
	 */
	public static <S extends Serializable, I extends Entity<S>> Map<S, I> toEntityMap(
			Collection<I> collection) {
		if ((collection == null) || collection.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<S, I> result = createLinkedHashMap(collection.size());
		for (I instance : collection) {
			result.put(instance.getId(), instance);
		}
		return result;
	}

	/**
	 * Converts the given list of entities to entity mapping where the key is the entity id and the
	 * value is the id itself.
	 * 
	 * @param <S>
	 *            the entity id type
	 * @param <I>
	 *            the entity type
	 * @param list
	 *            the list
	 * @param from
	 *            the start index to begin collecting (including)
	 * @param to
	 *            the end index to finish collecting (excluding)
	 * @return the map
	 */
	public static <S extends Serializable, I extends Entity<S>> Map<S, I> toEntityMap(List<I> list,
			int from, int to) {
		if ((list == null) || list.isEmpty()) {
			return Collections.emptyMap();
		}
		int end = Math.min(to, list.size());
		int start = Math.max(from, 0);
		Map<S, I> result = createLinkedHashMap(end - start);
		for (int i = start; i < end; i++) {
			I element = list.get(i);
			result.put(element.getId(), element);
		}

		return result;
	}
	
	/**
	 * Check whether a collection is null or empty.
	 * 
	 * @param collection
	 *            Collection to chech.
	 * @return {@code true} if the collection is {@code null} or
	 *         {@link Collection#isEmpty()} returns {@code true}.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
}
