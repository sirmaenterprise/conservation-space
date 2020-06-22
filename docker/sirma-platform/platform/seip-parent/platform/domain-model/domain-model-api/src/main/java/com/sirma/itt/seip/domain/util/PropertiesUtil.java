package com.sirma.itt.seip.domain.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Utility class for common method for working with instance properties.
 *
 * @author BBonev
 */
public class PropertiesUtil {

	private static final String[] NON_PRINTABLE_PROPERTIES = new String[] { DefaultProperties.THUMBNAIL_IMAGE,
			DefaultProperties.CONTENT };

	/**
	 * Instantiates a new properties util.
	 */
	private PropertiesUtil() {
		// utility class
	}

	/**
	 * Clean null properties from the given model.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @return the e
	 */
	public static <E extends PropertyModel> E cleanNullProperties(E model) {
		for (Iterator<Entry<String, Serializable>> it = model.getProperties().entrySet().iterator(); it.hasNext();) {
			Entry<String, Serializable> entry = it.next();
			if (entry.getValue() == null || entry.getValue() instanceof String && "null".equals(entry.getValue())) {
				it.remove();
			}
		}
		return model;
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 *
	 * @param original
	 *            the original map to copy
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original) {
		return cloneProperties(original, Collections.<String> emptySet());
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 *
	 * @param original
	 *            the original map to copy
	 * @param skipKeys
	 *            the skip keys
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original, String... skipKeys) {
		return cloneProperties(original,
				skipKeys == null ? Collections.<String> emptySet() : new LinkedHashSet<>(Arrays.asList(skipKeys)));
	}

	/**
	 * Shallow-copies to a new map except for maps and collections that are binary serialized.
	 *
	 * @param original
	 *            the original map to copy
	 * @param skipKeys
	 *            the skip keys
	 * @return a copy of the source map
	 */
	public static Map<String, Serializable> cloneProperties(Map<String, Serializable> original, Set<String> skipKeys) {
		// Copy the values, ensuring that any collections are copied as well
		Map<String, Serializable> copy = CollectionUtils.createLinkedHashMap(original.size());
		for (Map.Entry<String, Serializable> element : original.entrySet()) {
			String key = element.getKey();
			if (skipKeys.contains(key)) {
				continue;
			}
			Serializable value = element.getValue();
			if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
				value = (Serializable) SerializationUtils.deserialize(SerializationUtils.serialize(value));
			} else if (value instanceof Instance && value instanceof Cloneable) {
				// the only way to guarantee the object separation is via
				// cloning of the object
				value = (Serializable) ReflectionUtils.invokeNoArgsMethod(value, "clone");
			}
			copy.put(key, value);
		}
		return copy;
	}

	/**
	 * Merge properties from the source map to the destination map. If the value of the source and destination map is of
	 * type {@link PropertyModel} then model properties will be merged also. The method copies missing properties from
	 * the source to the destination but could also override all properties from the source map if the argument
	 * {@code overrideExisting} is equal to <code>true</code>.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param source
	 *            the source map to get properties from
	 * @param destination
	 *            the destination map to write the result
	 * @param overrideExisting
	 *            if <code>true</code> the source will be copied to the destination map if full.
	 * @return true, if the destination map has been modified in any way.
	 */
	public static <K, V> boolean mergeProperties(Map<K, V> source, Map<K, V> destination, boolean overrideExisting) {
		if (source == null || destination == null) {
			// nothing to do
			return false;
		}
		boolean modified = false;
		for (Entry<K, V> entry : source.entrySet()) {
			V destValue = destination.get(entry.getKey());
			V sourceValue = entry.getValue();
			if (destValue instanceof PropertyModel && sourceValue instanceof PropertyModel) {
				modified |= mergeProperties(((PropertyModel) sourceValue).getProperties(),
						((PropertyModel) destValue).getProperties(), overrideExisting);
			} else if (destValue == null || overrideExisting) {
				destination.put(entry.getKey(), sourceValue);
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Prints the given map as String but before that removes any non printable properties
	 *
	 * @param properties
	 *            the properties
	 * @return the string
	 */
	public static String toString(Map<String, Serializable> properties) {
		if (properties == null) {
			return null;
		}
		// this is not very effective due to the map copy
		// maybe we should copy the original AbstractMap.toString method and update it
		Map<String, Serializable> copy = new HashMap<>(properties);
		for (String property : NON_PRINTABLE_PROPERTIES) {
			if (copy.containsKey(property)) {
				copy.put(property, "<" + property + "_value>");
			}
		}
		return copy.toString();
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
	 * Copy value.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param key
	 *            the key
	 */
	public static void copyValue(Instance source, Instance target, String key) {
		CollectionUtils.copyValue(source.getProperties(), target.getProperties(), key);
	}

	/**
	 * Copy value from the source instance property to the target instance only if the value exists in the source
	 * instance property.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	public static boolean copyValueIfExist(Instance source, Instance target, String key) {
		return CollectionUtils.copyValueIfExist(source.getProperties(), target.getProperties(), key);
	}
}
