package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sirma.itt.commons.utils.string.StringUtils;
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
	 * Adds the given string value if not null or not empty string.
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	default boolean addIfNotNullOrEmpty(String name, String value) {
		return addIf(name, value, StringUtils::isNotNullOrEmpty);
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
