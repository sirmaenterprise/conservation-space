/*
 *
 */
package com.sirma.itt.seip.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Collections utils class similar to {@link java.util.Collections}. The provided empty collections here are not
 * immutable and are suitable for web use.
 *
 * @author BBonev
 */
public class CollectionUtils {

	private static final String CLONE_METHOD_NAME = "clone";
	/**
	 * The size increase of the created sets and maps to compensate the not full load factor of the maps and sets.
	 */
	private static final float SIZE_NORMALIZATION = 1.1f;
	/** The load factor for maps and sets. */
	private static final float LOAD_FACTOR = 0.95f;

	@SuppressWarnings("rawtypes")
	private static final Map EMPTY_MAP = new SealedMap<>(null, true);

	@SuppressWarnings("rawtypes")
	private static final Set EMPTY_SET = new SealedSet<>(null, true);

	@SuppressWarnings("rawtypes")
	private static final List EMPTY_LIST = new SealedList<>(null, true);

	private static final Consumer<?> EMPTY_CONSUMER = e -> {
		return;
	};

	/*
	 * the variables are with this names because if are the same the sonar analysis breaks
	 */
	private static final BiFunction<Object, Object, Object> FIRST_FUNCTION = (f1, s1) -> f1;
	private static final BiFunction<Object, Object, Object> SECOND_FUNCTION = (f2, s2) -> s2;

	public static final Predicate<?> IS_EMPTY_MAP = arg -> arg == null || ((Map<?, ?>) arg).isEmpty();

	/**
	 * Instantiates a new collection utils.
	 */
	private CollectionUtils() {
		// utility class
	}

	/**
	 * Gets unmodifiable empty map that does not throw exceptions on modifications in contrast of
	 * {@link Collections#emptyMap()}
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the empty map
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> emptyMap() {
		return EMPTY_MAP;
	}

	/**
	 * Gets unmodifiable empty set that does not throw exceptions on modifications in contrast of
	 * {@link Collections#emptySet()}
	 *
	 * @param <E>
	 *            the element type
	 * @return the sets the
	 */
	@SuppressWarnings("unchecked")
	public static <E> Set<E> emptySet() {
		return EMPTY_SET;
	}

	/**
	 * Gets unmodifiable empty list that does not throw exceptions on modifications in contrast of
	 * {@link Collections#emptyList()}
	 *
	 * @param <E>
	 *            the element type
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <E> List<E> emptyList() {
		return EMPTY_LIST;
	}

	/**
	 * Calls the {@link Collection#toArray(Object[])} method constructing array based on the given argument type.
	 *
	 * @param <E>
	 *            the element type
	 * @param collection
	 *            the collection
	 * @param type
	 *            the type
	 * @return the e[]
	 */
	@SuppressWarnings("unchecked")
	public static <E> E[] toArray(Collection<? extends E> collection, Class<E> type) {
		return collection.toArray((E[]) Array.newInstance(type, collection.size()));
	}

	/**
	 * Adds the value to map. If the key does not exists new value list is created and the value is added to it. The
	 * method ignores null values
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
		if (newValue == null) {
			return;
		}
		map.computeIfAbsent(key, k -> new LinkedList<>()).add(newValue);
	}

	/**
	 * Adds the value to map. If the key does not exists new value set is created and the value is added to it. The
	 * method ignores null values
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
		if (newValue == null) {
			return;
		}
		map.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(newValue);
	}

	/**
	 * Creates the linked hash map instance that will hold the given amount of elements. The returned instance is
	 * optimized for that size and will not grow until the given number of elements are added.
	 * <p>
	 * The method is same as <code>new LinkedHashMap&lt;K, V&gt;((int) (size * 1.1), 0.95f)</code>
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
		return new LinkedHashMap<>((int) (size * SIZE_NORMALIZATION), LOAD_FACTOR);
	}

	/**
	 * Create a supplier that provides {@link LinkedHashMap} of the given size.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the size of the new map
	 * @return the supplier
	 * @see #createLinkedHashMap(int)
	 */
	public static <K, V> Supplier<Map<K, V>> provideLinkedHashMap(int size) {
		return () -> createLinkedHashMap(size);
	}

	/**
	 * Creates new case insensitive linked hash map instance that will hold the given amount of elements. The returned
	 * instance is optimized for that size and will not grow until the given number of elements are added.
	 *
	 * @param size
	 *            the preferred size
	 * @param <V>
	 *            the value type
	 * @return the map instance
	 */
	public static <V> Map<String, V> createCaseInsensitiveLinkedHashMap(int size) {
		return new CaseInsensitiveLinkedHashMap<>((int) (size * SIZE_NORMALIZATION), LOAD_FACTOR);
	}

	/**
	 * Creates the hash map instance that will hold the given amount of elements. The returned instance is optimized for
	 * that size and will not grow until the given number of elements are added.
	 * <p>
	 * The method is same as <code>new HashMap&lt;K, V&gt;((int) (size * 1.1), 0.95f)</code>
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
		return new HashMap<>((int) (size * SIZE_NORMALIZATION), LOAD_FACTOR);
	}

	/**
	 * Create a supplier that provides a {@link HashMap} of the given size
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the size of the new map
	 * @return the supplier
	 * @see #createHashMap(int)
	 */
	public static <K, V> Supplier<Map<K, V>> provideHashMap(int size) {
		return () -> createHashMap(size);
	}

	/**
	 * Creates the hash set instance that will hold the given amount of elements. The returned instance is optimized for
	 * that size and will not grow until the given number of elements are added.
	 * <p>
	 * The method is same as <code>new HashSet&lt;V&gt;((int) (size * 1.1), 0.95f)</code>
	 *
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <V> Set<V> createHashSet(int size) {
		return new HashSet<>((int) (size * SIZE_NORMALIZATION), LOAD_FACTOR);
	}

	/**
	 * Creates a supplier that provides a {@link HashSet} of the given size.
	 *
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the size of the new set
	 * @return the supplier
	 * @see #createHashSet(int)
	 */
	public static <V> Supplier<Set<V>> provideHashSet(int size) {
		return () -> createHashSet(size);
	}

	/**
	 * Creates the linked hash set instance that will hold the given amount of elements. The returned instance is
	 * optimized for that size and will not grow until the given number of elements are added.
	 * <p>
	 * The method is same as <code>new LinkedHashSet&lt;V&gt;((int) (size * 1.1), 0.95f)</code>
	 *
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the preferred size
	 * @return the map instance
	 */
	public static <V> Set<V> createLinkedHashSet(int size) {
		return new LinkedHashSet<>((int) (size * SIZE_NORMALIZATION), LOAD_FACTOR);
	}

	/**
	 * Creates a supplier that provides a {@link LinkedHashSet} of the given size.
	 *
	 * @param <V>
	 *            the value type
	 * @param size
	 *            the size of the new set
	 * @return the supplier
	 * @see #createLinkedHashSet(int)
	 */
	public static <V> Supplier<Set<V>> provideLinkedHashSet(int size) {
		return () -> createLinkedHashSet(size);
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
		if (list == null || list.isEmpty() || list.size() <= fromIndex) {
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
		return copyValueIfExist(source, key, target, key);
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
	 * Copy value if it exists (is not <code>null</code>) from the source map.
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
	 * @return true, if successful
	 */
	public static <K, V> boolean copyValueIfExist(Map<K, V> source, K sourceKey, Map<K, V> target, K targetKey) {
		V v = source.get(sourceKey);
		if (v != null) {
			target.put(targetKey, v);
			return true;
		}
		return false;
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
	 * <b>NOTE:</b> for optimal results if possible construct your collection only with lower/upper case strings. It's
	 * going to be faster if the checks need to be performed often.
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
	 * Adds elements to a collection. The method modifies the supplied collection by adding the elements from the
	 * variable arguments parameter if any.
	 *
	 * @param <C>
	 *            the collection type
	 * @param <E>
	 *            the elements type
	 * @param collection
	 *            the collection to add the elements to. The collection implementation should support <code>null</code>
	 *            values if such are expected in the second argument.
	 * @param elements
	 *            a list of elements to add to the specified collection. If any of the elements is <code>null</code> it
	 *            will be added to the collection.
	 * @return the same collection instance as the argument with added elements
	 */
	@SafeVarargs
	public static <E, C extends Collection<E>> C addToCollection(C collection, E... elements) {
		if (collection == null || elements == null || elements.length == 0) {
			return collection;
		}
		for (E e : elements) {
			collection.add(e);
		}
		return collection;
	}

	/**
	 * Adds the given {@link Pair} arguments to the given map. If the map is <code>null</code> new one will be created
	 * for the given arguments. If both arguments are <code>null</code> {@link Collections#EMPTY_MAP} will be returned.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map to populate if present or <code>null</code> to create new one using the method
	 *            {@link #createLinkedHashMap(int)}
	 * @param pairs
	 *            the pairs to add to the map. The first element of the pair will be the key and the second value.
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
	 * Appends value to collection. If value is collection all elements are added, if value is object it is simply
	 * added. If skipDuplicates is set to true, objects already contained in the collection are not added again. Note
	 * that element would be type casted to collection type.
	 *
	 * @param data
	 *            is the collection to update - set, list, etc. Should be modifiable
	 * @param value
	 *            is value of type T or {@link Collection} of elements of type T
	 * @param skipDuplicates
	 *            whether to treat data as {@link Set}. Note if data is already {@link Set} parameter does not affect
	 *            the result. Note if data already contains duplicate elements they are not affected.
	 * @return the updated data collection
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> addValue(Collection<T> data, Object value, boolean skipDuplicates) {
		if (value instanceof Collection) {
			Collection<T> toBeAdded = (Collection<T>) value;
			if (skipDuplicates && !(data instanceof Set)) {
				Set<T> nonDuplicates = new LinkedHashSet<>(toBeAdded);
				nonDuplicates.removeAll(data);
				data.addAll(nonDuplicates);
			} else {
				data.addAll(toBeAdded);
			}
		} else if (value != null) {
			if (skipDuplicates && !(data instanceof Set) && data.contains(value)) {
				return data;
			}
			data.add((T) value);
		}
		return data;
	}

	/**
	 * Converts the given collection to entity mapping where the key is the entity id of the given instance and the
	 * value entity the instance itself.
	 *
	 * @param <S>
	 *            the entity id type
	 * @param <I>
	 *            the entity type
	 * @param collection
	 *            the collection
	 * @return the map
	 */
	public static <S extends Serializable, I extends Entity<S>> Map<S, I> toEntityMap(Collection<I> collection) {
		if (collection == null || collection.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<S, I> result = createLinkedHashMap(collection.size());
		for (I instance : collection) {
			result.put(instance.getId(), instance);
		}
		return result;
	}

	/**
	 * Check whether a collection is null or empty.
	 *
	 * @param collection
	 *            Collection to check.
	 * @return {@code true} if the collection is {@code null} or {@link Collection#isEmpty()} returns {@code true}.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Returns a predicated that tests the argument passed is null or empty collection.
	 *
	 * @param <E>
	 *            the element type (should be {@link Collection})
	 * @return the that test for empty collection
	 */
	public static <E> Predicate<E> isEmptyCollection() {
		return arg -> arg == null || ((Collection<?>) arg).isEmpty();
	}

	/**
	 * Returns a predicate that checks if the map argument is empty or <code>null</code>
	 *
	 * @param <E>
	 *            the element type (should be {@link Map})
	 * @return the predicate that tests for empty map
	 */
	@SuppressWarnings("unchecked")
	public static <E> Predicate<E> isEmptyMap() {
		return (Predicate<E>) IS_EMPTY_MAP;
	}

	/**
	 * Checks if is null or empty.
	 *
	 * @param map
	 *            the map
	 * @return true, if is empty
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Checks if given array is empty or null.
	 *
	 * @param objects
	 *            array to check
	 * @return true if null or its length is 0, false otherwise
	 */
	public static boolean isEmpty(Object[] objects) {
		return objects == null || objects.length == 0;
	}

	/**
	 * Checks if is not null or empty.
	 *
	 * @param map
	 *            the map
	 * @return true, if is not empty
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * Converts the given list of entities to entity mapping where the key is the entity id and the value is the entity
	 * itself.
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
	public static <S extends Serializable, I extends Entity<S>> Map<S, I> toEntityMap(List<I> list, int from, int to) {
		if (list == null || list.isEmpty()) {
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
	 * Check that a collection is not null or empty.
	 *
	 * @param c
	 *            Collection to check.
	 * @return {@code true} if the collection is not {@code null} or empty.
	 */
	public static boolean isNotEmpty(Collection<?> c) {
		return !CollectionUtils.isEmpty(c);
	}

	/**
	 * Adds the given value to the collection if the value is not <code>null</code>.
	 *
	 * @param <E>
	 *            the collection element type
	 * @param collection
	 *            the collection to add the value to
	 * @param value
	 *            the value to add
	 * @return <code>true</code>, if the collection has been modified by the operation and <code>false</code> if the
	 *         value was <code>null</code> or the collection already contained the value
	 */
	public static <E> boolean addNonNullValue(Collection<E> collection, E value) {
		if (value == null) {
			return false;
		}
		return collection.add(value);
	}

	/**
	 * Adds the given value to the collection if the value satisfies the given predicate.
	 *
	 * @param <E>
	 *            the collection element type
	 * @param collection
	 *            the collection to add the value to
	 * @param value
	 *            the value to add
	 * @param predicate
	 *            the predicate to test againts the given value
	 * @return <code>true</code>, if the collection has been modified by the operation and <code>false</code> if the
	 *         value was <code>null</code> or the collection already contained the value
	 */
	public static <E> boolean addIf(Collection<E> collection, E value, Predicate<E> predicate) {
		if (predicate.test(value)) {
			return collection.add(value);
		}
		return false;
	}

	/**
	 * Adds the given string value to the given collection only if the given string value is not <code>null</code> or
	 * empty string.
	 *
	 * @param collection
	 *            the collection to add to
	 * @param value
	 *            the value to add
	 * @return <code>true</code>,if the collection has been modified by the operation and <code>false</code> if the
	 *         value was <code>null</code> or the collection already contained the value
	 */
	public static boolean addNonNullValue(Collection<String> collection, String value) {
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return collection.add(value);
	}

	/**
	 * Adds the given element to the given {@link Stream.Builder} only if the given value is not <code>null</code>.
	 *
	 * @param <T>
	 *            value type
	 * @param builder
	 *            the target builder
	 * @param value
	 *            the value to add
	 * @return <code>true</code>,if the element was passed to the builder and <code>false</code> if the value was
	 *         <code>null</code>
	 */
	public static <T> boolean addNonNullValue(Builder<T> builder, T value) {
		if (value == null) {
			return false;
		}
		builder.add(value);
		return true;
	}

	/**
	 * Adds the non null value to a map for the given key
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return <code>true</code>, if the map has been modified by the operation and <code>false</code> if the value was
	 *         <code>null</code>
	 */
	public static <K, V> boolean addNonNullValue(Map<K, V> map, K key, V value) {
		if (value == null) {
			return false;
		}
		map.put(key, value);
		return true;
	}

	/**
	 * Adds the given value with the given key to the map if the key is not found in the map or the value was
	 * <code>null</code>. The method performs a {@link Map#get(Object)} and then {@link Map#put(Object, Object)} if the
	 * found value is <code>null</code>
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return <code>true</code>, if successful the map was modified as a result of the operation
	 */
	public static <K, V> boolean addIfAbsent(Map<K, V> map, K key, V value) {
		V oldValue = map.get(key);
		if (oldValue == null) {
			map.put(key, value);
			return true;
		}
		return false;
	}

	/**
	 * Intersection of two sets. The returned set will contain only the elements contained in both Collections
	 *
	 * @param <T>
	 *            the value type
	 * @param toBeModified
	 *            the to be modified
	 * @param matchAgainst
	 *            the match against
	 * @return a set that contains the elements from toBeModified that are common with matchAgainst
	 * @see Set#retainAll(Collection)
	 */
	public static <T> Set<T> intersection(Collection<T> toBeModified, Collection<T> matchAgainst) {
		if (isEmpty(toBeModified) || isEmpty(matchAgainst)) {
			return Collections.emptySet();
		}
		Set<T> set = new HashSet<>(toBeModified);
		set.retainAll(matchAgainst);
		return set;
	}

	/**
	 * Intersection of two sets containing elements of different types. The transform function will be applied to each
	 * element in the matchAgainst. The returned set will contain only the elements contained in both Collections.
	 *
	 * @param <T>
	 *            target collection element type
	 * @param <K>
	 *            source collection element type
	 * @param toBeModified
	 *            the collection that is going to be modified
	 * @param matchAgainst
	 *            the collection that is going to be transformed and matched against
	 * @param transform
	 *            the transform function
	 * @return a set that contains the elements from toBeModified that are common with matchAgainst
	 * @see Set#retainAll(Collection)
	 */
	public static <T, K> Set<T> intersection(Collection<T> toBeModified, Collection<K> matchAgainst,
			Function<K, T> transform) {
		Objects.requireNonNull(transform, "Transform function is required!");
		if (isEmpty(toBeModified) || isEmpty(matchAgainst)) {
			return Collections.emptySet();
		}

		Set<T> result = new HashSet<>(matchAgainst.size());
		for (K item : matchAgainst) {
			result.add(transform.apply(item));
		}
		return intersection(toBeModified, result);
	}

	/**
	 * Removes the duplicates by modifying the source list and keeping the order of the elements.
	 *
	 * @param <T>
	 *            the generic type
	 * @param list
	 *            the list
	 * @return the same object as the argument
	 */
	public static <T> List<T> removeDuplicates(List<T> list) {
		Set<T> temp = createLinkedHashSet(list.size());
		temp.addAll(list);
		list.clear();
		list.addAll(temp);
		return list;
	}

	/**
	 * Returns the given collection as stream.
	 *
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param parallel
	 *            <code>true</code> if the stream should be a parallel stream
	 * @return the stream
	 */
	public static <T> Stream<T> asStream(Collection<T> source, boolean parallel) {
		return parallel ? source.parallelStream() : source.stream();
	}

	/**
	 * Transforms the given iterator to list
	 *
	 * @param <T>
	 *            the generic type
	 * @param iterator
	 *            the iterator
	 * @return the list
	 */
	public static <T> List<T> toList(Iterator<T> iterator) {
		return toCollection(iterator, ArrayList::new);
	}

	/**
	 * Converts the given iterator to collection created via the provided {@link Supplier}.
	 * <p>
	 * Example use: <code>List&lt;String&gt; list = CollectionUtils.toCollection(iterator, LinkedList::new);</code>
	 *
	 * @param <T>
	 *            the generic type
	 * @param <C>
	 *            the generic type
	 * @param iterator
	 *            the iterator
	 * @param newInstance
	 *            the new instance
	 * @return the c
	 */
	public static <T, C extends Collection<T>> C toCollection(Iterator<T> iterator, Supplier<C> newInstance) {
		Objects.requireNonNull(iterator, "The iterator is required");
		Objects.requireNonNull(newInstance, "New Instance supplier is required");

		C list = newInstance.get();
		iterator.forEachRemaining(list::add);
		return list;
	}

	/**
	 * Returns a collector that computes the distribution of the provided elements. This effectively counts how many
	 * times an item has appeared in a stream.
	 *
	 * @param <T>
	 *            the counted type
	 * @return the distribution collector
	 */
	public static <T> Collector<T, ?, Map<T, Long>> distribution() {
		return Collectors.groupingBy(Function.identity(), Collectors.counting());
	}

	/**
	 * Select random element from the given collection.
	 *
	 * @param <T>
	 *            the collection type
	 * @return random element from the collection
	 */
	public static <T> Function<Collection<T>, T> randomPicker() {
		return c -> {
			if (c.isEmpty()) {
				return null;
			}
			if (c.size() == 1) {
				return c.iterator().next();
			}
			SecureRandom random = new SecureRandom();
			random.nextInt();
			Iterator<T> it = c.iterator();
			int randomIndex = random.nextInt(c.size());
			int index = 0;
			while (it.hasNext() && index++ < randomIndex) {
				it.next();
			}
			return it.next();
		};
	}

	/**
	 * Creates c map collector that maps the value with the stream contest and the key is determined by the given key
	 * mapper function.<br>
	 * The method is identical of calling: <code>Collectors.toMap(keyMapper, Function.identity())</code>
	 *
	 * @param <T>
	 *            the generic type
	 * @param <K>
	 *            the key type
	 * @param keyMapper
	 *            the key mapper
	 * @return the collector
	 */
	public static <T, K> Collector<T, ?, Map<K, T>> toIdentityMap(Function<? super T, ? extends K> keyMapper) {
		return Collectors.toMap(keyMapper, Function.identity());
	}

	/**
	 * Creates c map collector that maps the value with the stream contest and the key is determined by the given key
	 * mapper function.<br>
	 * The method is identical of calling: <code>Collectors.toMap(keyMapper, Function.identity())</code>
	 *
	 * @param <T>
	 *            the generic type
	 * @param <K>
	 *            the key type
	 * @param keyMapper
	 *            the key mapper
	 * @param mapSupplier
	 *            the new map supplier
	 * @return the collector
	 */
	public static <T, K> Collector<T, ?, Map<K, T>> toIdentityMap(Function<? super T, ? extends K> keyMapper,
			Supplier<Map<K, T>> mapSupplier) {
		return Collectors.toMap(keyMapper, Function.identity(), throwingMerger(), mapSupplier);
	}

	/**
	 * Returns a merge function, suitable for use in {@link Map#merge(Object, Object, BiFunction) Map.merge()} or
	 * {@link Collectors#toMap(Function, Function, BinaryOperator) toMap()}, The returned operator will always throw
	 * {@code IllegalStateException}. This can be used to enforce the assumption that the elements being collected are
	 * distinct.
	 *
	 * @param <T>
	 *            the type of input arguments to the merge function
	 * @return a merge function which always throw {@code IllegalStateException}
	 */
	public static <T> BinaryOperator<T> throwingMerger() {
		return (u, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", u));
		};
	}

	/**
	 * Converts the given collection to identity map. The map keys are determined by the given key mapper and the values
	 * are the collection elements. Note that if keys are not unique then exception will be thrown.<br>
	 * The method is identical of calling:
	 * <code>source.stream().collect(CollectionUtils.toIdentityMap(keyMapper))</code>
	 *
	 * @param <T>
	 *            the generic type
	 * @param <K>
	 *            the key type
	 * @param source
	 *            the source
	 * @param keyMapper
	 *            the key mapper
	 * @return the map
	 */
	public static <T, K> Map<K, T> toIdentityMap(Collection<T> source, Function<? super T, ? extends K> keyMapper) {
		return source.stream().collect(toIdentityMap(keyMapper));
	}

	/**
	 * Empty consumer that does nothing.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the consumer
	 */
	@SuppressWarnings("unchecked")
	public static <T> Consumer<T> emptyConsumer() {
		return (Consumer<T>) EMPTY_CONSUMER;
	}

	/**
	 * Always returns the first argument of the given binary function. Can be used for
	 * {@link Map#merge(Object, Object, BiFunction)} method or
	 * {@link Collectors#toMap(Function, Function, java.util.function.BinaryOperator)}
	 *
	 * @param <V>
	 *            the value type
	 * @return the bi function
	 */
	@SuppressWarnings("unchecked")
	public static <V> BiFunction<V, V, V> useFirst() {
		return (BiFunction<V, V, V>) FIRST_FUNCTION;
	}

	/**
	 * Always returns the second argument of the given binary function. Can be used for
	 * {@link Map#merge(Object, Object, BiFunction)} method or
	 * {@link Collectors#toMap(Function, Function, java.util.function.BinaryOperator)}
	 *
	 * @param <V>
	 *            the value type
	 * @return the bi function
	 */
	@SuppressWarnings("unchecked")
	public static <V> BiFunction<V, V, V> useSecond() {
		return (BiFunction<V, V, V>) SECOND_FUNCTION;
	}

	/**
	 * Transform the source collection using the mapping function to convert the elements to the output type. The result
	 * is written to a set.
	 *
	 * @param <T>
	 *            the source type type
	 * @param <R>
	 *            the result type
	 * @param source
	 *            the source
	 * @param mapping
	 *            the mapping used for the value transformation
	 * @return the sets the
	 */
	public static <T, R> Set<R> transformToSet(Collection<T> source, Function<T, R> mapping) {
		return source.stream().map(mapping).collect(Collectors.toCollection(() -> createLinkedHashSet(source.size())));
	}

	/**
	 * Transform the source collection using the mapping function to convert the elements to the output type. The result
	 * is written to a list.
	 *
	 * @param <T>
	 *            the source type type
	 * @param <R>
	 *            the result type
	 * @param source
	 *            the source
	 * @param mapping
	 *            the mapping used for the value transformation
	 * @return the list
	 */
	public static <T, R> List<R> transformToList(Collection<T> source, Function<T, R> mapping) {
		Objects.requireNonNull(mapping, "Mapping functions is required!");
		if (isEmpty(source)) {
			return new LinkedList<>();
		}
		List<R> result = new LinkedList<>();
		for (T item : source) {
			result.add(mapping.apply(item));
		}
		return result;
	}

	/**
	 * Returns a {@code Collector} that accumulates the input elements into a new {@code List}, in encounter order. If
	 * the provided size is greater than zero then {@link ArrayList} with the given size will be created otherwise a
	 * {@link LinkedList} will be created for undermined size.
	 *
	 * @param <T>
	 *            the type of the input elements
	 * @param <C>
	 *            the type of the resulting {@code Collection}
	 * @param expectedSize
	 *            the expected size of the collection if known. If not known a -1 could be passed.
	 * @return a {@code Collector} which collects all the input elements into a {@code List}, in encounter order
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends Collection<T>> Collector<T, ?, C> toList(int expectedSize) {
		if (expectedSize > 0) {
			return (Collector<T, ?, C>) Collectors.toCollection(() -> new ArrayList<>(expectedSize));
		}
		return (Collector<T, ?, C>) Collectors.toCollection(LinkedList::new);
	}

	/**
	 * Merge the contents of the second collection into the first and return the first collection. Can be used to merge
	 * collections for {@link Map#merge(Object, Object, BiFunction)}.
	 *
	 * @param <E>
	 *            the collection element type
	 * @param <C>
	 *            the collection type
	 * @param destination
	 *            the destination to update
	 * @param source
	 *            the source to add the the destination
	 * @return the destination collection
	 */
	public static <E, C extends Collection<E>> C merge(C destination, C source) {
		if (source != null) {
			destination.addAll(source);
		}
		return destination;
	}

	/**
	 * Deep clone the map and it's values. Not clonable values will be the same as original. Non clonable
	 * map/collections will be copied to new map/collections of similar type.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param toClone
	 *            the to clone
	 * @return the map
	 */
	public static <K, V> Map<K, V> clone(Map<K, V> toClone) {
		if (toClone == null) {
			return null;
		}
		Map<K, V> copy = cloneContainer(toClone);
		for (Entry<K, V> entry : toClone.entrySet()) {
			copy.put(cloneInternal(entry.getKey()), cloneInternal(entry.getValue()));
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	private static <V, K> Map<K, V> cloneContainer(Map<K, V> toClone) {
		Map<K, V> copy;
		if (toClone instanceof Cloneable) {
			copy = (Map<K, V>) ReflectionUtils.invokeNoArgsMethod(toClone, CLONE_METHOD_NAME);
			copy.clear();
		} else {
			// this case is when we have unmodifiable map
			copy = createLinkedHashMap(toClone.size());
		}
		return copy;
	}

	/**
	 * Deep clone collection and it's values. Not clonable values will be the same as original. Non clonable
	 * map/collections will be copied to new map/collections of similar type
	 *
	 * @param <V>
	 *            the value type
	 * @param <C>
	 *            the generic type
	 * @param toClone
	 *            the to clone
	 * @return the c
	 */
	public static <V, C extends Collection<V>> C clone(C toClone) {
		if (toClone == null) {
			return null;
		}
		C copy = cloneContainer(toClone);
		for (V value : toClone) {
			// deep clone collections and maps
			copy.add(cloneInternal(value));
		}
		return copy;
	}

	/**
	 * Shuffle the list using {@link Collections#shuffle(List, Random)}. This method is placed here
	 * for convenience since there is no Collections.unshuffle and we want both methods to come from
	 * the same place.
	 *
	 * @see {@link Collections#shuffle(List, Random)}
	 * @param toShuffle
	 *            the list to shuffle
	 * @param random
	 *            the seed
	 */
	public static <V, C extends List<V>> void shuffle(C toShuffle, Random random) {
		Collections.shuffle(toShuffle, random);
	}

	/**
	 * Deshuffle the list using the Fisher-Yates shuffle (The same algorithm used in
	 * {@link Collections#shuffle(List, Random)}). What that gives us is a way to reverse the
	 * shuffle process and return the collection to it's previous state based on the seed that was
	 * used to shuffle it in the first place.
	 *
	 * @param toDeshuffle
	 *            the list to deshuffle
	 * @param random
	 *            the seed that was used to shuffle the list
	 */
	public static <V, C extends List<V>> void deshuffle(C toDeshuffle, Random random) {
		int[] randoms = new int[toDeshuffle.size() - 1];
		int j = 0;
		for (int i = toDeshuffle.size() - 1; i > 0; i--) {
			randoms[j++] = random.nextInt(i + 1);
		}

		// deShuffling
		for (int i = 1; i < toDeshuffle.size(); i++) {
			// use the random values backwards
			int index = randoms[toDeshuffle.size() - i - 1];
			// simple swap
			V a = toDeshuffle.get(index);
			toDeshuffle.set(index, toDeshuffle.get(i));
			toDeshuffle.set(i, a);
		}
	}

	@SuppressWarnings("unchecked")
	private static <C extends Collection<V>, V> C cloneContainer(C toClone) {
		C copy;
		if (toClone instanceof Cloneable) {
			copy = (C) ReflectionUtils.invokeNoArgsMethod(toClone, CLONE_METHOD_NAME);
			copy.clear();
		} else if (toClone instanceof RandomAccess) {
			copy = (C) new ArrayList<V>();
		} else if (toClone instanceof Set<?>) {
			copy = (C) createLinkedHashSet(toClone.size());
		} else {
			// this case is when we have unmodifiable list
			copy = (C) new LinkedList<V>();
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	private static <V> V cloneInternal(V value) {
		if (value instanceof Collection<?>) {
			return (V) clone((Collection<?>) value);
		} else if (value instanceof Map<?, ?>) {
			return (V) clone((Map<?, ?>) value);
		} else if (value instanceof Cloneable) {
			return (V) ReflectionUtils.invokeNoArgsMethod(value, CLONE_METHOD_NAME);
		}
		return value;
	}

}
