package com.sirma.itt.seip.search;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single raw projection row result from a search operation.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/06/2017
 */
public interface ResultItem extends Iterable<ResultValue>, Serializable {

	/**
	 * Constant used to identify the property name used in group by search operations. Requesting this property
	 * when no search aggregation is done will have unspecified value
	 */
	String GROUP_BY_NAME = "$groupBy$"; // NOSONAR
	/**
	 * Constant used to identify the property value returned from search aggregation. Requesting this property
	 * when no search aggregation is done will have unspecified value
	 */
	String GROUP_BY_VALUE = "name"; // NOSONAR
	/**
	 * Constant used to identify the property value count returned from search aggregation. Requesting this property
	 * when no search aggregation is done will have unspecified value
	 */
	String GROUP_BY_COUNT = "count"; // NOSONAR

	/**
	 * Creates an iterator over the values in this ResultItem. This only returns non-null
	 * values. An implementation is free to return the values in arbitrary order.
	 */
	Iterator<ResultValue> iterator();

	/**
	 * Gets the names of the values in this ResultItem.
	 *
	 * @return A set of binding names.
	 */
	Set<String> getValueNames();

	/**
	 * Checks whether this ResultItem has a value with the specified name.
	 *
	 * @param name The name of the binding.
	 * @return <tt>true</tt> if this ResultItem has a value with the specified name, <tt>false</tt>
	 * otherwise.
	 */
	boolean hasValue(String name);

	/**
	 * Gets the value of the value with the specified name from this ResultItem.
	 *
	 * @param name The name of the name-value pair.
	 * @return The value of the binding with the specified name, or <tt>null</tt> if there is no such value
	 * in this ResultItem.
	 */
	ResultValue getValue(String name);

	/**
	 * Gets the actual value for the given projection name.
	 *
	 * @param name the name of the name-value pair to get
	 * @return the actual value for the specified name or null if there is no such value in the projection
	 */
	Serializable getResultValue(String name);

	/**
	 * Gets a result value as the given type by casting to the target type. If the value is not the expected type a
	 * {@link ClassCastException} will be thrown. If the value is null then the default value will be returned
	 *
	 * @param name the result item name to fetch
	 * @param type the expected type
	 * @param defaultValue the default value to return if not found
	 * @param <T> the expected type
	 * @return the value cast to the expected type
	 */
	default <T> T getAs(String name, Class<T> type, T defaultValue) {
		Serializable value = getResultValue(name);
		if (value != null) {
			return type.cast(value);
		}
		return defaultValue;
	}

	/**
	 * Gets a string value or null
	 *
	 * @param name the property name to fetch
	 * @return the string value
	 */
	default String getString(String name) {
		return getAs(name, String.class, null);
	}

	/**
	 * Gets a boolean value or false if not found
	 *
	 * @param name the property name to fetch
	 * @return the boolean value
	 */
	default boolean getBooleanOrFalse(String name) {
		return getAs(name, Boolean.class, Boolean.FALSE);
	}

	/**
	 * Gets a boolean value or true if not found
	 *
	 * @param name the property name to fetch
	 * @return the boolean value
	 */
	default boolean getBooleanOrTrue(String name) {
		return getAs(name, Boolean.class, Boolean.TRUE);
	}

	/**
	 * Gets a boolean value or the provided default value, count be null
	 *
	 * @param name the property name to fetch
	 * @param defaultValue the default value to return if the requested value is not found
	 * @return the found value or the default value
	 */
	default Boolean getBoolean(String name, Boolean defaultValue) {
		return getAs(name, Boolean.class, defaultValue);
	}

	/**
	 * Gets a integer value or zero if not found
	 *
	 * @param name the name of the property to fetch
	 * @return the found value or zero
	 */
	default int getInt(String name) {
		return getAs(name, Integer.class, 0);
	}

	/**
	 * Gets a integer value or the given default value
	 *
	 * @param name the name of the property to fetch
	 * @param defaultValue the value to return if the given name is not found or null
	 * @return the found value or the given default value
	 */
	default Integer getInt(String name, Integer defaultValue) {
		return getAs(name, Integer.class, defaultValue);
	}

	/**
	 * Gets a long value or zero
	 *
	 * @param name the name of the property to fetch
	 * @return the found value or zero
	 */
	default long getLong(String name) {
		return getAs(name, Long.class, 0L);
	}

	/**
	 * Gets a long value or the given default value
	 *
	 * @param name the name of the property to fetch
	 * @param defaultValue the value to return if the given name is not found or null
	 * @return the found value or the given default value
	 */
	default Long getLong(String name, Long defaultValue) {
		return getAs(name, Long.class, defaultValue);
	}

	/**
	 * Gets a property value that corresponds to the group by mapping for the result stream. <br>This is the name of
	 * the property used for grouping.
	 *
	 * @return the group by property or null if not applicable
	 */
	default String getGroupBy() {
		return Objects.toString(getResultValue(GROUP_BY_NAME), null);
	}
	/**
	 * Gets a property value that corresponds to the group by name mapping for the result stream. <br>This is the
	 * value that was counted and corresponds to the property returned from {@link #getGroupBy()}
	 *
	 * @return the group by property value or null if not applicable
	 */
	default String getGroupByValue() {
		return Objects.toString(getResultValue(GROUP_BY_VALUE), null);
	}
	/**
	 * Gets a property value that corresponds to the group by count for the result stream. <br>This is the
	 * found count of values after performing the grouping over the property value returned from {@link #getGroupByValue()}
	 *
	 * @return the count of group by values or 0 if not applicable
	 */
	default int getGroupByCount() {
		return getInt(GROUP_BY_COUNT);
	}

	/**
	 * Returns the number of values in this ResultItem.
	 *
	 * @return The number of values in this ResultItem.
	 */
	int size();

	/**
	 * Compares a ResultItem object to another object.
	 *
	 * @param o The object to compare this result item to.
	 * @return <tt>true</tt> if the other object is an instance of {@link ResultValue} and it contains the same
	 * set of values (disregarding order), <tt>false</tt> otherwise.
	 */
	boolean equals(Object o);

	/**
	 * The hash code of a value is defined as the bit-wise XOR of the hash codes of its values:
	 * <p>
	 * <pre>
	 * int hashCode = 0;
	 *
	 * for (ResultValue item : this) {
	 * 	hashCode &circ;= item.getName().hashCode() &circ; item.getValue().hashCode();
	 * }
	 * </pre>
	 * <p>
	 * Note: the calculated hash code intentionally does not depend on the order in which the name-value are
	 * iterated over.
	 *
	 * @return A hash code for the BindingSet.
	 */
	int hashCode();
}
