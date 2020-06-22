package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Interface that defines methods to add a custom properties support to any object and convenient method for modifying
 * of the properties
 *
 * @author BBonev
 */
public interface Properties extends PropertiesReader {

	/**
	 * Sets the properties.
	 *
	 * @param properties
	 *            the properties
	 */
	void setProperties(Map<String, Serializable> properties);

	/**
	 * Gets or create properties for the current {@link Properties} instance.
	 *
	 * @return non <code>null</code> properties map
	 */
	default Map<String, Serializable> getOrCreateProperties() {
		Map<String, Serializable> map = getProperties();
		if (map == null) {
			map = new HashMap<>();
			setProperties(map);
		}
		return map;
	}

	/**
	 * Adds all of the given properties to the current model. The method will add and override properties and will not
	 * remove any.
	 *
	 * @param newProperties
	 *            the new properties to add.
	 */
	default void addAllProperties(Map<String, ? extends Serializable> newProperties) {
		if (newProperties != null) {
			getOrCreateProperties().putAll(newProperties);
		}
	}

	/**
	 * Removes any properties with keys that match the elements of the given collection. If the given set is
	 * <code>null</code> or empty nothing will be changed.
	 *
	 * @param propertyKeysToRemove
	 *            the property keys to remove
	 */
	default void removeProperties(Collection<String> propertyKeysToRemove) {
		if (propertyKeysToRemove != null && !propertyKeysToRemove.isEmpty()) {
			Map<String, Serializable> properties = getProperties();
			if (properties != null) {
				properties.keySet().removeAll(propertyKeysToRemove);
			}
		}
	}

	/**
	 * Removes a property identified by the given name.
	 *
	 * @param name
	 *            the property name
	 * @return the property value or <code>null</code> if not found, properties are <code>null</code> or the value
	 *         itself was <code>null</code> if the target {@link Map} implementation supports <code>null</code> values.
	 */
	default Serializable remove(String name) {
		Map<String, Serializable> properties = getProperties();
		if (properties == null) {
			return null;
		}
		return properties.remove(name);
	}

	/**
	 * Removes a value from a property only if the current value matches or contains the given value. In other words if
	 * the current value is not the expected value then the operation does nothing.
	 * <ul>
	 *     <li>if the current value is null or it's not present then the operation does nothing</li>
	 *     <li>if the removed value is null then the operation does nothing</li>
	 *     <li>if the property has a single value and that value matches the one expected then the key and the value are removed from the properties map</li>
	 *     <li>if the current property value is a collection with a single value and that value is the expected one then the key and the whole collection will be removed from the properties map</li>
	 *     <li>if the current property value is a collection with more then one elements and one of them is the expected then that element will be removed from the collection and the remaining values put back in the properties map</li>
	 *     <li>if the current value is a collection and the expected value is not present the method does nothing</li>
	 *     <li>if the current value is a single value and does not match the expected value then the method does nothing</li>
	 * </ul>
	 * @param name the name of the property to remove
	 * @param value is the expected value contained for the given property
	 * @return true if the operation modified the instance properties in any way
	 */
	default boolean remove(String name, Serializable value){
		Serializable serializable = get(name);
		if (serializable == null) {
			return false;
		}
		boolean changed = false;
		if (serializable instanceof Collection) {
			Collection<Serializable> collectionValue = (Collection<Serializable>) serializable;
			if (collectionValue.contains(value)) {
				if (collectionValue.size() <= 1) {
					remove(name);
					changed = true;
				} else {
					collectionValue.remove(value);
					if (collectionValue.size() == 1) {
						serializable = collectionValue.iterator().next();
					}
					add(name, serializable);
					changed = true;
				}
			}
		} else if (serializable.equals(value)) {
			remove(name);
			changed = true;
		}
		return changed;
	}

	/**
	 * Adds the the given value to properties only if the given predicate returns <code>true</code>. If the current
	 * properties are not initialized they will be initialized.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the name
	 * @param value
	 *            the value to add
	 * @param predicate
	 *            the predicate to test if the value is applicable for adding
	 * @return true, if successful
	 */
	default <T extends Serializable> boolean addIf(String name, T value, Predicate<T> predicate) {
		if (predicate.test(value)) {
			add(name, value);
			return true;
		}
		return false;
	}

	/**
	 * Adds the the given value to properties only if the given predicate returns <code>true</code>. If the current
	 * properties are not initialized they will be initialized.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the name
	 * @param value
	 *            the value to add
	 * @param predicate
	 *            the predicate to test if the key and value are applicable for adding
	 * @return true, if successful
	 */
	default <T extends Serializable> boolean addIf(String name, T value, BiPredicate<String, T> predicate) {
		if (predicate.test(name, value)) {
			add(name, value);
			return true;
		}
		return false;
	}

	/**
	 * Adds the the given value to properties only if the given property is not already in the properties map.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the name
	 * @param value
	 *            the value to add
	 * @return true, if modification was done to the properties
	 */
	default <T extends Serializable> boolean addIfNotPresent(String name, T value) {
		return addIf(name, value, (key, v) -> !isPropertyPresent(key));
	}

	/**
	 * Adds a value mapped to a key to the properties map if the key is mapped to {@code null}.
	 *
	 * @param <T>
	 *            the type of the value to add
	 * @param name
	 *            Property key.
	 * @param value
	 *            Property value.
	 * @return true, if modification was done to the properties
	 */
	default <T extends Serializable> boolean addIfNullMapping(String name, T value) {
		return addIf(name, value, (key, v) -> isValueNull(key));
	}

	/**
	 * Adds a property to current properties map. If the properties are not initialized they will be.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the property name
	 * @param value
	 *            the value to add
	 * @return the previous value if or <code>null</code>
	 */
	default <T extends Serializable> Serializable add(String name, T value) {
		return getOrCreateProperties().put(name, value);
	}

	/**
	 * Add value to a property without replacing the current value. Applicable for multi value fields.
	 * <ul>
	 *     <li>if the given value is null then the method does nothing</li>
	 *     <li>if the given property does not have any value then the property value will be the given value placed in a collection</li>
	 *     <li>if the current value is a single value then it will be placed in a collection with the given value, only if the values are different</li>
	 *     <li>if the current value is a collection of values then the given value will be added to the collection if it's not already in the collection</li>
	 * </ul>
	 * @param name is the property name to update
	 * @param value is the value to append
	 * @return {@code true} if the property value changed from the method call and {@code false} if call didn't affect the stored data
	 */
	@SuppressWarnings("unchecked")
	default boolean append(String name, Serializable value) {
		Objects.requireNonNull(name, "name is required");
		if (value == null) {
			return false;
		}

		Serializable currentValue = get(name);

		if (currentValue instanceof Collection) {
			if (!((Collection) currentValue).contains(value)) {
				return ((Collection<Serializable>) currentValue).add(value);
			}
			return false;
		} else {
			add(name, new LinkedList<>());
			append(name, currentValue);
		}
		return append(name, value);
	}

	/**
	 * Add multiple values to a property without replacing the current value. Applicable for multi value fields.
	 * <ul>
	 *     <li>if the given value is null {@link NullPointerException} will be thrown</li>
	 *     <li>if the given property does not have any value then the property value will be a copy of the given collection of values</li>
	 *     <li>if the current value is a single value then it will be placed in a collection along with all of the given values that are not already present in that collection. For example if the current value is '1' and we are adding values ['2', '2'] then the result collection will have the values ['1', '2']</li>
	 *     <li>if the current value is a collection of values then the given values will be added to the collection if they are not already in the collection</li>
	 * </ul>
	 * @param name is the property name to update
	 * @param values are the values to append
	 * @return {@code true} if the property value changed from the method call and {@code false} if call didn't affect the stored data
	 */
	default <T extends Serializable> boolean appendAll(String name, Collection<? extends T> values) {
		Objects.requireNonNull(name, "name is required");
		Objects.requireNonNull(values, "values is required");

		return values.stream().map(value -> append(name, value)).reduce(Boolean.FALSE, (b1, b2) -> b1 || b2);
	}

	/**
	 * Adds the given string value if not null or not empty string.
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	default boolean addIfNotNullOrEmpty(String name, String value) {
		return addIf(name, value, StringUtils::isNotBlank);
	}

	/**
	 * Adds the given value if it's not null.
	 *
	 * @param <T>
	 *            the value type
	 * @param name
	 *            the name
	 * @param value
	 *            the value to add
	 * @return true, if successful
	 */
	default <T extends Serializable> boolean addIfNotNull(String name, T value) {
		return addIf(name, value, Objects::nonNull);
	}

	/**
	 * Prevent further modifications to the properties of this object.
	 */
	default void preventModifications() {
		Map<String, Serializable> properties = getProperties();
		if (CollectionUtils.isEmpty(properties)) {
			setProperties(Collections.emptyMap());
		} else {
			setProperties(Collections.unmodifiableMap(properties));
		}
	}

	/**
	 * Transform a property with specified name by applying the given transformer function and storing the result in the
	 * properties map again if non <code>null</code>. This effectively reads a property, converts its value and store it
	 * back in the map of properties with the same key.
	 *
	 * @param name
	 *            the name of the key to transform
	 * @param transformer
	 *            the transformer to apply to the value if found. The transformer will not be called with null value
	 * @return true, if value was found and the result of the transformer function was non <code>null</code>.
	 */

	default boolean transform(String name, Function<Serializable, Serializable> transformer) {
		return addIfNotNull(name, getAs(name, transformer));
	}
}
