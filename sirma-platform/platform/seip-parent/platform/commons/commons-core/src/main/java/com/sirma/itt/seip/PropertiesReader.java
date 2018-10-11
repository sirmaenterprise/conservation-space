package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Allows an object expose properties mapping. The interface provides methods for various ways of accessing the generic
 * properties in safe manner. Provided access is only for read only purposes. The map returned from the
 * {@link #getProperties()} may or may not allow modifications of the contents. <br>
 * All access methods can handle <code>null</code> value from the {@link #getProperties()} method. They will return the
 * appropriate default value in this case.
 *
 * @author BBonev
 */
public interface PropertiesReader {

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	Map<String, Serializable> getProperties();

	/**
	 * Gets the boolean property or false if not found or not a boolean
	 *
	 * @param name
	 *            the property name
	 * @return the boolean property
	 */
	default boolean getBoolean(String name) {
		return getBoolean(name, () -> false);
	}

	/**
	 * Gets the boolean property or the given default value if not found or not a boolean
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if the property is not found
	 * @return the boolean property
	 */
	default boolean getBoolean(String name, boolean defaultValue) {
		return getBoolean(name, () -> defaultValue);
	}

	/**
	 * Gets the boolean property or the value produces by calling the given supplier.
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to be called if value is not found
	 * @return the boolean property
	 */
	default boolean getBoolean(String name, BooleanSupplier defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.getAsBoolean();
		}
		Serializable value = map.get(name);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		if (value != null) {
			if ("true".equalsIgnoreCase(value.toString())) {
				return true;
			}
			if ("false".equalsIgnoreCase(value.toString())) {
				return false;
			}
		}
		return defaultValueSupplier.getAsBoolean();
	}

	/**
	 * Gets the int property or false if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @return the int property
	 */
	default int getInt(String name) {
		return getInt(name, () -> 0);
	}

	/**
	 * Gets the int property or the given default value if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if the property is not found
	 * @return the int property
	 */
	default int getInt(String name, int defaultValue) {
		return getInt(name, () -> defaultValue);
	}

	/**
	 * Gets the int property or the value produces by calling the given supplier.
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to be called if value is not found
	 * @return the int property
	 */
	default int getInt(String name, IntSupplier defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.getAsInt();
		}
		Serializable value = map.get(name);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return defaultValueSupplier.getAsInt();
	}

	/**
	 * Gets the long property or false if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @return the long property
	 */
	default long getLong(String name) {
		return getLong(name, () -> 0);
	}

	/**
	 * Gets the long property or the given default value if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if the property is not found
	 * @return the long property
	 */
	default long getLong(String name, long defaultValue) {
		return getLong(name, () -> defaultValue);
	}

	/**
	 * Gets the long property or the value produces by calling the given supplier.
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to be called if value is not found
	 * @return the long property
	 */
	default long getLong(String name, LongSupplier defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.getAsLong();
		}
		Serializable value = map.get(name);
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return defaultValueSupplier.getAsLong();
	}

	/**
	 * Gets the double property or false if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @return the double property
	 */
	default double getDouble(String name) {
		return getDouble(name, () -> 0.0);
	}

	/**
	 * Gets the double property or the given default value if not found or not a number
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if the property is not found
	 * @return the double property
	 */
	default double getDouble(String name, double defaultValue) {
		return getDouble(name, () -> defaultValue);
	}

	/**
	 * Gets the double property or the value produces by calling the given supplier.
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to be called if value is not found
	 * @return the double property
	 */
	default double getDouble(String name, DoubleSupplier defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.getAsDouble();
		}
		Serializable value = map.get(name);
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return defaultValueSupplier.getAsDouble();
	}

	/**
	 * Gets the string property or <code>null</code> if not found or not a string
	 *
	 * @param name
	 *            the property name
	 * @return the string property or <code>null</code>
	 */
	default String getString(String name) {
		return getString(name, () -> null);
	}

	/**
	 * Gets the string property or the given default value if not found or not a string
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if the property is not found or not a string
	 * @return the string property
	 */
	default String getString(String name, String defaultValue) {
		return getString(name, () -> defaultValue);
	}

	/**
	 * Gets the string property or the value from the given supplier if value is not found or not a string
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to use to produce a default value if property not found or not a string
	 * @return the string property
	 */
	default String getString(String name, Supplier<String> defaultValueSupplier) {
		return get(name, String.class, defaultValueSupplier);
	}

	/**
	 * Gets the property or null if not found
	 *
	 * @param name
	 *            the property name
	 * @return the property value or <code>null</code>
	 */
	default Serializable get(String name) {
		return get(name, () -> null);
	}

	/**
	 * Gets the property value of the given default value
	 *
	 * @param name
	 *            the property name
	 * @param defaultValue
	 *            the default value to return if property not found
	 * @return the property value or the default value
	 */
	default Serializable get(String name, Serializable defaultValue) {
		return get(name, () -> defaultValue);
	}

	/**
	 * Gets the property value or executes the given supplier to produce the default value.
	 *
	 * @param name
	 *            the property name
	 * @param defaultValueSupplier
	 *            the default value supplier to be executed if property value is <code>null</code>
	 * @return the property value or the result of calling the given supplier
	 */
	default Serializable get(String name, Supplier<Serializable> defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.get();
		}
		Serializable serializable = map.get(name);
		if (serializable != null) {
			return serializable;
		}
		return defaultValueSupplier.get();
	}

	/**
	 * Gets the property value only if the property exists and is of the desired type otherwise <code>null</code> will
	 * be returned.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the property name
	 * @param ofType
	 *            the desired type
	 * @return the property value or the result of calling the given supplier
	 */
	default <T> T get(String name, Class<T> ofType) {
		return get(name, ofType, () -> null);
	}

	/**
	 * Gets the property value only if the property exists and is of the desired type otherwise <code>null</code> will
	 * be returned.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the property name
	 * @param ofType
	 *            the desired type
	 * @param defaultValue
	 *            the default value to be used if property value is <code>null</code>
	 * @return the property value or the result of calling the given supplier
	 */
	default <T> T getOrDefault(String name, Class<T> ofType, T defaultValue) {
		return get(name, ofType, () -> defaultValue);
	}

	/**
	 * Gets the property value only if the property exists and is of the desired type otherwise the given supplier will
	 * be used to produce a value to be returned.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the property name
	 * @param ofType
	 *            the desired type
	 * @param defaultValueSupplier
	 *            the default value supplier to be executed if property value is <code>null</code>
	 * @return the property value or the result of calling the given supplier
	 */
	default <T> T get(String name, Class<T> ofType, Supplier<T> defaultValueSupplier) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValueSupplier.get();
		}
		Serializable serializable = map.get(name);
		if (ofType.isInstance(serializable)) {
			return ofType.cast(serializable);
		}
		return defaultValueSupplier.get();
	}

	/**
	 * Gets a value from the current properties and applies a transform function before returning value. The function is
	 * applied only if the value is non <code>null</code>. If properties are not initialized or the value is not set or
	 * null then <code>null</code> will be returned.
	 *
	 * @param <T>
	 *            the expected new type
	 * @param name
	 *            the name of the property to fetch
	 * @param transformFunction
	 *            the transform function to apply
	 * @return the transformed value or <code>null</code>
	 * @see #getAs(String, Function, Supplier)
	 */
	default <T> T getAs(String name, Function<? super Serializable, T> transformFunction) {
		return getAs(name, transformFunction, () -> null);
	}

	/**
	 * Gets a value from the current properties and applies a transform function before returning the value. The
	 * function is applied only if the value is non null. If properties are not initialized or the value is not set or
	 * null then the provided supplier will be used to return a default value.
	 *
	 * @param <T>
	 *            the expected new type
	 * @param name
	 *            the name of the property to fetch
	 * @param transformFunction
	 *            the transform function to apply
	 * @param defaultValue
	 *            the default value supplier to use
	 * @return the transformed value or the value provided from the default value supplier.
	 */
	default <T> T getAs(String name, Function<? super Serializable, T> transformFunction, Supplier<T> defaultValue) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return defaultValue.get();
		}
		Serializable serializable = map.get(name);
		if (serializable == null) {
			return defaultValue.get();
		}
		return transformFunction.apply(serializable);
	}

	/**
	 * Gets a property value as collection. If the value is not a collection it will be placed in one created via the
	 * provided supplier. if the value is already a {@link Collection} instance it will be returned as is. <br>
	 * Note that the method should not modify the internal structure in anyway.
	 *
	 * @param <E>
	 *            the collection element type
	 * @param <C>
	 *            the expected collection type
	 * @param name
	 *            the name of the property to retrieve
	 * @param defaultCollectionInit
	 *            the supplier will be used to provide new mutable instance in case the value does not exists or it's
	 *            not of type collection.
	 * @return the value as collection
	 */
	@SuppressWarnings("unchecked")
	default <E extends Serializable, C extends Collection<E>> Collection<E> getAsCollection(String name,
			Supplier<C> defaultCollectionInit) {
		return getAs(name, value -> {
			// if the value is already a collection return it as is
			if (value instanceof Collection) {
				return (C) value;
			}
			// otherwise add it to new collection of the provided type
			C newCollection = defaultCollectionInit.get();
			newCollection.add((E) value);
			return newCollection;
		}, defaultCollectionInit);
	}

	/**
	 * Gets the value identified by the given key as string. The value if exists will be converted to string by calling
	 * its toString() method. Note that this method is different from {@link #getString(String)} as this converts the
	 * value before return.
	 *
	 * @param name
	 *            the name of the property
	 * @return the value as string or <code>null</code> if no such property
	 */
	default String getAsString(String name) {
		return getAs(name, Object::toString);
	}

	/**
	 * Gets the value identified by the given key as string. The value if exists will be converted to string by calling
	 * its toString() method. Note that this method is different from {@link #getString(String, Supplier)} as this
	 * converts the value before return.
	 *
	 * @param name
	 *            the name
	 * @param defaultValue
	 *            the default value
	 * @return the value as string or value returned by the given supplier if no such property
	 */
	default String getAsString(String name, Supplier<String> defaultValue) {
		return getAs(name, Object::toString, defaultValue);
	}

	/**
	 * Gets the value identified by the given key as double. The value if exists will be converted to double by calling
	 * {@link Double#parseDouble(String)} method. Note that this method is different from {@link #getDouble(String)} as
	 * this converts the value before return.
	 *
	 * @param name
	 *            the name of the property
	 * @return the value as double or <code>null</code> if no such property
	 */
	default Double getAsDouble(String name) {
		return getAsDouble(name, () -> Double.valueOf(0.0));
	}

	/**
	 * Gets the value identified by the given key as double. The value if exists will be converted to double by calling
	 * {@link Double#parseDouble(String)} method. Note that this method is different from
	 * {@link #getDouble(String, DoubleSupplier)} as this converts the value before return.
	 *
	 * @param name
	 *            the name
	 * @param defaultValue
	 *            the default value
	 * @return the value as double or value returned by the given supplier if no such property
	 */
	default Double getAsDouble(String name, Supplier<Double> defaultValue) {
		return getAs(name, value -> {
			if (value instanceof Double) {
				return (Double) value;
			}
			return Double.valueOf(value.toString());
		} , defaultValue);
	}

	/**
	 * Checks if is property present.
	 *
	 * @param key
	 *            the key
	 * @return true, if is property present
	 */
	default boolean isPropertyPresent(String key) {
		Map<String, Serializable> map = getProperties();
		return map != null && map.containsKey(key);
	}

	/**
	 * Checks if the given property matches or contains the given value. The method supports checking for null value.<br>
	 * This method does not check for exact match of values and allows value variance. For more strict behaviour check {@link #matchesValue(String, Serializable)}.<br>
	 * Works on the following algorithm.
	 * <ul>
	 *     <li>If the current value is null or missing and the expected value is null then the method will return {@code true}</li>
	 *     <li>If the current value is not null and the given value is null then the method will return {@code false}</li>
	 *     <li>If the current value is a collection and the given value is not a collection then the method will return {@code true} only if the given value is contained in the collection</li>
	 *     <li>If the current value is a collection (c1) and the given value is also a collection (c2) then the result of the method will be {@code true} only if all elements of {@code c2} are contained in the {@code c1}</li>
	 *     <li>If the current value is a single value and the given value is a single value then the method will return P{@code true} only if both values are equal by their equal method</li>
	 *     <li>If the current value is single value and the given value is a collection then the method will return {@code true} only if the given collection is with a single element and that element equals the current value</li>
	 * </ul>
	 *
	 * @param key is the property to check if contains the given value
	 * @param expectedValue is the expected value for the given key
	 * @return {@code true} if the given value equals or is contained in the current value stored in the instance
	 * @see #matchesValue(String, Serializable)
	 */
	default boolean containsValue(String key, Serializable expectedValue) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return false;
		}
		Serializable currentValue = map.get(key);
		if (currentValue == null) {
			return expectedValue == null;
		}
		// current value is not null so no need to continue
		if (expectedValue == null) {
			return false;
		}
		if (currentValue instanceof Collection) {
			if (expectedValue instanceof Collection) {
				return ((Collection<?>) currentValue).containsAll((Collection<?>) expectedValue);
			}
			return ((Collection<?>) currentValue).contains(expectedValue);
		}
		if (expectedValue instanceof Collection && ((Collection<?>) expectedValue).size() == 1) {
			return ((Collection<?>) expectedValue).contains(currentValue);
		}
		return !(expectedValue instanceof Collection) && currentValue.equals(expectedValue);
	}

	/**
	 * Checks if the given value equals the current value for the given key. If current or expected value is a
	 * collection of single value then the method will report correct matching if these values are equal. Otherwise from
	 * that this method checks for strict equality. <br>
	 * For more relaxed matching see {@link #containsValue(String, Serializable)}.
	 *
	 * @param key is the property to check if contains the given value
	 * @param expectedValue is the expected value for the given key
	 * @return {@code true} only if both values are undefined ({@code null}) or equal to one another
	 * @see #containsValue(String, Serializable)
	 */
	default boolean matchesValue(String key, Serializable expectedValue) {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			return false;
		}
		Serializable currentValue = map.get(key);
		if (currentValue == null) {
			return expectedValue == null;
		}
		if (expectedValue == null) {
			return false;
		}
		Serializable expectedValueSingle = expectedValue;
		if (expectedValue instanceof Collection && ((Collection) expectedValue).size() == 1) {
			expectedValueSingle = (Serializable) ((Collection<?>) expectedValue).iterator().next();
		}
		Serializable currentValueSingle = currentValue;
		if (currentValue instanceof Collection && ((Collection) currentValue).size() == 1) {
			currentValueSingle = (Serializable) ((Collection<?>) currentValue).iterator().next();
		}
		return currentValueSingle.equals(expectedValueSingle);
	}

	/**
	 * Checks if is property value is {@code null}.
	 *
	 * @param key
	 *            mapped value key.
	 * @return true, if is property is {@code null} or the properties map is {@code null}.
	 */
	default boolean isValueNull(String key) {
		Map<String, Serializable> map = getProperties();
		return map == null || map.get(key) == null;
	}

	/**
	 * Checks if is property value is not {@code null}.
	 *
	 * @param key
	 *            mapped value key.
	 * @return true, if is property is not {@code null} and the properties map is not {@code null}.
	 */
	default boolean isValueNotNull(String key) {
		Map<String, Serializable> map = getProperties();
		return map != null && map.get(key) != null;
	}

}
