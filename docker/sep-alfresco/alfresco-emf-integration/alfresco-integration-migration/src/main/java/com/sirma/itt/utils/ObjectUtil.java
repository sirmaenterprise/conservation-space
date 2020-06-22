/**
 * Copyright (c) 2010 01.10.2010 , Sirma ITT.
 */
package com.sirma.itt.utils;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for base object operations as checking objects state or
 * validity.
 *
 * @author B.Bonev
 */
public class ObjectUtil {
	/**
	 * Checks if the given string object is not <code>null</code> and does not
	 * have a zero length.
	 *
	 * @param s
	 *            is the object to check
	 * @return <code>true</code> if the argument is not <code>null</code> and
	 *         has non zero length.
	 */
	public static boolean isValid(String s) {
		return (s != null) && !s.isEmpty();
	}

	/**
	 * Checks if the given object is <code>null</code>.
	 *
	 * @param o
	 *            is the object to check
	 * @return <code>true</code> if the object is not <code>null</code>.
	 */
	public static boolean isValid(Object o) {
		return o != null;
	}

	/**
	 * Checks if the given collection is not <code>null</code> and is not empty.
	 *
	 * @param c
	 *            is the collection to check
	 * @return <code>true</code> if the collection is not <code>null</code> and
	 *         is not empty.
	 */
	public static boolean isValid(Collection<?> c) {
		return (c != null) && !c.isEmpty();
	}

	/**
	 * Checks if the given Map is not <code>null</code> and is not empty.
	 *
	 * @param c
	 *            is the map to check
	 * @return <code>true</code> if the collection is not <code>null</code> and
	 *         is not empty.
	 */
	public static boolean isValid(Map<?, ?> c) {
		return (c != null) && !c.isEmpty();
	}

	/**
	 * Checks the dates ranges if they are valid. The dates are considered valid
	 * if both are not <code>null</code> and start date is before end date. They
	 * also can be equal to each other.
	 *
	 * @param start
	 *            is the start date
	 * @param end
	 *            is the end date
	 * @return <code>true</code> if the dates are valid and <code>false</code>
	 *         otherwise
	 */
	public static boolean isValid(Date start, Date end) {
		if ((start != null) && (end != null)
				&& (start.equals(end) || start.before(end))) {
			return true;
		} else if ((start != null) ^ (end != null)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns a not <code>null</code> {@link Map} value of the given map. If
	 * the key is not in the map then new element is put in it and the value to
	 * it is returned by the method.
	 *
	 * @param <K>
	 *            is the map key
	 * @param <V>
	 *            is the list value type
	 * @param <S>
	 *            sub map key
	 * @param <T>
	 *            sub map value list type
	 * @param map
	 *            is the map to check in
	 * @param key
	 *            is the key to check
	 * @return a value references from the map.
	 */
	public static <K, V, S, T> Map<S, List<T>> getNotNullMap(
			Map<K, Map<S, List<T>>> map, K key) {
		Map<S, List<T>> subMap = map.get(key);
		if (subMap == null) {
			subMap = new LinkedHashMap<S, List<T>>();
			map.put(key, subMap);
		}
		return subMap;
	}

	/**
	 * Returns a not <code>null</code> {@link List} value of the given map. If
	 * the key is not in the map then new element is put in it and the value to
	 * it is returned by the method.
	 *
	 * @param <S>
	 *            is the map key
	 * @param <T>
	 *            is the list value type
	 * @param map
	 *            is the map to check in
	 * @param key
	 *            is the key to check
	 * @return a value references from the map.
	 */
	public static <S, T> List<T> getNotNullList(Map<S, List<T>> map, S key) {
		List<T> list = map.get(key);
		if (list == null) {
			list = new LinkedList<T>();
			map.put(key, list);
		}
		return list;
	}

	/**
	 * Creates new class instance using his default constructor using
	 * reflection.
	 *
	 * @param <E>
	 *            is the class type to create
	 * @param clazz
	 *            is the defining class
	 * @return the newly created object instance.
	 */
	public static <E> E createInstance(Class<E> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
