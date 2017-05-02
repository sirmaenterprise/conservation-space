package com.sirma.itt.seip.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.IndexTransformer;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverterUtil;

/**
 * Utility class for helper methods for working with JSON objects REVIEW - 1. may be move in emf core 2. exception could
 * be thrown if key is not found
 *
 * @author BBonev
 */
public class JsonUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

	/**
	 * Instantiates a new json util.
	 */
	private JsonUtil() {
		// utility class
	}

	/**
	 * Adds the to JSON object.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void addToJson(JSONObject object, String key, Object value) {
		if (object == null || key == null) {
			return;
		}
		try {
			object.put(key, value);
		} catch (JSONException e) {
			LOGGER.warn("Failed to set entry with key {} and value {}", key, value, e);
			try {
				// XXX: Why?!
				object.put(key, (String) null);
			} catch (JSONException e1) {
				LOGGER.warn("Failed to put entry with key {} and value null", key, e1);
			}
		}
	}

	/**
	 * Adds the to json.
	 *
	 * @param array
	 *            the array
	 * @param value
	 *            the value
	 */
	public static void addToJson(JSONArray array, Object value) {
		array.put(value);
	}

	/**
	 * Adds the to json.
	 *
	 * @param array
	 *            the array
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	public static void addToJson(JSONArray array, int index, Object value) {
		try {
			array.put(index, value);
		} catch (JSONException e) {
			LOGGER.error("Failed to put to json array", e);
		}
	}

	/**
	 * Copy to json.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source, String sourceKey) {
		if (object == null || key == null) {
			return;
		}
		copyToJson(object, key, source, sourceKey, null);
	}

	/**
	 * Copy to json.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source) {
		if (object == null || key == null) {
			return;
		}
		copyToJson(object, key, source, key, null);
	}

	/**
	 * Copy to json.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param targetClass
	 *            the target class
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source, Class<?> targetClass) {
		if (object == null || key == null) {
			return;
		}
		copyToJson(object, key, source, key, targetClass);
	}

	/**
	 * Copy to json.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 * @param targetClass
	 *            the target class
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source, String sourceKey,
			Class<?> targetClass) {
		if (object == null || key == null) {
			return;
		}
		if (source != null) {
			String localKey = sourceKey;
			if (localKey == null) {
				localKey = key;
			}
			Object value = source.get(localKey);
			if (targetClass != null) {
				value = TypeConverterUtil.getConverter().convert(targetClass, value);
			}
			addToJson(object, key, value);
		}
	}

	/**
	 * Appends the given value to the {@link org.json.JSONArray} that is represented by the given key.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param value
	 *            the value to append
	 */
	public static void append(JSONObject object, String key, Object value) {
		if (object == null || key == null) {
			return;
		}
		try {
			object.append(key, value);
		} catch (JSONException e) {
			LOGGER.warn("Failed to append to entry with key " + key + " and value " + value, e);
			try {
				object.append(key, (String) null);
			} catch (JSONException e1) {
				LOGGER.warn("Failed to append to entry with key " + key + " and value null", e1);
			}
		}
	}

	/**
	 * Gets a long value from the JSON object with a key if present
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the long value
	 */
	public static Long getLongValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Long.valueOf(value.toString());
		}
		return null;
	}

	/**
	 * Gets a double value from the JSON object with a key if present
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the double value
	 */
	public static Double getDoubleValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Double.valueOf(value.toString());
		}
		return null;
	}

	/**
	 * Gets a String value from the JSON object with a key if present
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the string value
	 */
	public static String getStringValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	/**
	 * Gets an integer value from the JSON object with a key if present
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the integer value
	 */
	public static Integer getIntegerValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Integer.valueOf(value.toString());
		}
		return null;
	}

	/**
	 * Gets the boolean value.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the boolean value or null if not found
	 */
	public static Boolean getBooleanValue(JSONObject object, String key) {
		return getBooleanValue(object, key, null);
	}

	/**
	 * Gets the boolean value from the given json object or the given default value
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the boolean value
	 */
	public static Boolean getBooleanValue(JSONObject object, String key, Boolean defaultValue) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Boolean.valueOf(value.toString());
		}
		return defaultValue;
	}

	/**
	 * Gets the value from the JSON object if present or <code>null</code> if not
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key to check and retrieve
	 * @return the value or <code>null</code> if not present
	 */
	public static Object getValueOrNull(JSONObject object, String key) {
		if (object == null) {
			return null;
		}
		if (object.has(key)) {
			try {
				Object value = object.get(key);
				boolean isNotNull = !(value instanceof String
						&& ("null".compareTo((String) value) == 0 || "".equals(value)) || value == JSONObject.NULL);
				if (isNotNull) {
					return value;
				}
			} catch (JSONException e) {
				LOGGER.warn("Failed to get value with key " + key, e);
			}
		}
		return null;
	}

	/**
	 * Retrieves a string value from a {@link JSONArray} at a specified index.
	 *
	 * @param array
	 *            Array from which to get the value.
	 * @param index
	 *            Index of the value in the array.
	 * @return The string value of the element at the specified index or null if there was an error while retrieving the
	 *         value.
	 */
	public static String getStringFromArray(JSONArray array, int index) {
		return array.optString(index, null);
	}

	/**
	 * Retrieves an element of a json array at a specified index and casts it to a specified type.
	 *
	 * @param <T>
	 *            Type to cast to.
	 * @param array
	 *            JSON array to retrieve from.
	 * @param index
	 *            Index of the desired element.
	 * @param returnType
	 *            Type to cast to.
	 * @return {@code null} if the element is null or there was a problem retrieving the element.
	 */
	public static <T> T getFromArray(JSONArray array, int index, Class<T> returnType) {
		return returnType.cast(array.opt(index));
	}

	/**
	 * Get value from array by index
	 *
	 * @param array
	 *            Array to get from
	 * @param index
	 *            Element index
	 * @return Element or null there was an exception durring retrieval.
	 */
	public static Object getFromArray(JSONArray array, int index) {
		return getFromArray(array, index, Object.class);
	}

	/**
	 * Gets the JSONObject from the given object or <code>null</code> if the result is not an {@link JSONObject}
	 * instance.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the json object or <code>null</code>
	 */
	public static JSONObject getJsonObject(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		return null;
	}

	/**
	 * Gets the JSONArray from the given object or <code>null</code> if the result is not an {@link JSONArray} instance.
	 *
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the json array or <code>null</code>
	 */
	public static JSONArray getJsonArray(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		return null;
	}

	/**
	 * Converts the given data to {@link JSONArray}. If the data is invalid empty array will be returned.
	 *
	 * @param data
	 *            the data
	 * @return the jSON array
	 */
	public static JSONArray toJsonArray(String data) {
		try {
			return new JSONArray(data);
		} catch (JSONException e) {
			LOGGER.warn("Json array data was invalid: " + data, e);
		}
		// return empty array if the data is invalid not to break anything
		return new JSONArray();
	}

	/**
	 * Copies missing properties from the given source to destination.
	 *
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 */
	public static void copyMissingProperties(JSONObject source, JSONObject destination) {
		for (Iterator<?> it = source.keys(); it.hasNext();) {
			String type = (String) it.next();
			if (!destination.has(type)) {
				try {
					destination.put(type, source.get(type));
				} catch (JSONException e) {
					LOGGER.warn("Failed to copy property " + type, e);
				}
			}
		}
	}

	/**
	 * Generates modelMap to return in the modelAndView in case of exception.
	 *
	 * @param msg
	 *            message
	 * @return the string
	 */
	public static JSONObject mapError(String msg) {

		JSONObject modelMap = new JSONObject();
		addToJson(modelMap, "message", msg);
		addToJson(modelMap, "success", Boolean.FALSE);
		return modelMap;
	}

	/**
	 * Parses a string to a {@link JSONArray}.
	 *
	 * @param arrayString
	 *            String to parse.
	 * @return {@link JSONArray} representation of the string, or null if there was an error during parse.
	 */
	public static JSONArray createArrayFromString(String arrayString) {
		try {
			return new JSONArray(arrayString);
		} catch (JSONException e) {
			LOGGER.warn("Failed to parse string {}", arrayString, e);
		}
		return null;
	}

	/**
	 * Parses a string to a {@link JSONObject}.
	 *
	 * @param objectString
	 *            String representing a JSON object.
	 * @return {@link JSONObject} representation of the string, or null if there was an error during parse.
	 */
	public static JSONObject createObjectFromString(String objectString) {
		if (objectString == null) {
			return null;
		}
		try {
			return new JSONObject(objectString);
		} catch (JSONException e) {
			LOGGER.warn("Failed to parse string {}", objectString, e);
		}
		return null;
	}

	/**
	 * Builds a {@link JSONObject} from string or map. If the value is not of those types, convert to String is
	 * attempted
	 *
	 * @param value
	 *            the value to convert
	 * @return the JSON object or null on error
	 */
	public static JSONObject toJsonObject(Serializable value) {
		if (value == null) {
			return null;
		}
		try {
			if (value instanceof String) {
				return new JSONObject(value.toString());
			}
			if (value instanceof Map) {
				return JsonUtil.toJsonObject((Map<?, ?>) value);
			}
			String converted = TypeConverterUtil.getConverter().convert(String.class, value);
			if (converted != null) {
				return new JSONObject(converted);
			}
		} catch (Exception e) {
			LOGGER.error("Error during convert to JSONObject of {}", value, e);
		}
		return null;
	}

	/**
	 * To json object.
	 *
	 * @param map
	 *            the map
	 * @return the JSON object
	 */
	public static JSONObject toJsonObject(Map<?, ?> map) {
		try {
			return map != null ? new JSONObject(map) : null;
		} catch (Exception e) {
			LOGGER.error("Error during convert to JSONObject of {}", map, e);
		}
		return null;
	}

	/**
	 * From Json to map object.
	 *
	 * @param jsonObject
	 *            the json object
	 * @return the map
	 */
	public static Map<String, Object> jsonToMap(JSONObject jsonObject) {
		Map<String, Object> map = new HashMap<>();
		Iterator<?> interator = jsonObject.keys();
		while (interator.hasNext()) {
			String key = (String) interator.next();
			Object value = getValueOrNull(jsonObject, key);
			if (value instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) value;
				map.put(key, jsonArrayToList(jsonArray));
			} else {
				map.put(key, getValueOrNull(jsonObject, key));
			}
		}
		return map;
	}

	/**
	 * Json array to list.
	 *
	 * @param jsonArray
	 *            the json array
	 * @return the list
	 */
	public static List<?> jsonArrayToList(JSONArray jsonArray) {
		return TypeConverterUtil.getConverter().convert(List.class, jsonArray);
	}

	/**
	 * Checks if is null or empty.
	 *
	 * @param o
	 *            the o
	 * @return true, if is null or empty
	 */
	public static boolean isNullOrEmpty(JSONObject o) {
		return o == null || o.length() == 0;
	}

	/**
	 * Checks if is null or empty.
	 *
	 * @param a
	 *            the a
	 * @return true, if is null or empty
	 */
	public static boolean isNullOrEmpty(JSONArray a) {
		return a == null || a.length() == 0;
	}

	/**
	 * Checks if the given string represents a {@link JSONArray}.
	 *
	 * @param data
	 *            the data
	 * @return true, if is array
	 */
	public static boolean isArray(String data) {
		String trimmed = org.apache.commons.lang.StringUtils.trimToNull(data);
		return trimmed != null && trimmed.startsWith("[") && trimmed.endsWith("]");
	}

	/**
	 * Checks if the given string represents a {@link JSONObject}.
	 *
	 * @param data
	 *            the data
	 * @return true, if is object
	 */
	public static boolean isJsonObject(String data) {
		String trimmed = org.apache.commons.lang.StringUtils.trimToNull(data);
		return trimmed != null && trimmed.startsWith("{") && trimmed.endsWith("}");
	}

	/**
	 * Converts the given {@link Context} object to {@link JSONObject} by converting all keys to string using toString
	 * method and all values to be JSON compatible using the method {@link #convertToJsonCompatibleValue(Object)}.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param context
	 *            the context
	 * @return the JSON object
	 */
	public static <K, V> JSONObject toJsonObject(Context<K, V> context) {
		if (context == null) {
			return null;
		}
		JSONObject object = new JSONObject();
		for (Entry<K, V> entry : context.entrySet()) {
			String key = entry.getKey().toString();
			V value = entry.getValue();
			Object converted = convertToJsonCompatibleValue(value);
			if (converted != null) {
				addToJson(object, key, converted);
			} else {
				LOGGER.warn("Context convert detected not supported value for key {}={}", key, entry.getValue());
			}
		}
		return object;
	}

	/**
	 * Converts the given object to JSON compatible value. If the value is {@link JsonRepresentable} uses the returned
	 * value. Values of type {@link Number}, {@link String}, {@link Boolean}, {@link JSONObject} and {@link JSONArray}
	 * are left intact. Dates are converted to ISO format. All other values are tried to be converted to
	 * {@link JSONObject} and to {@link String}. Any collections are converted to {@link JSONArray}s and all elements
	 * are converted using the current method. Any map values are converted using the current method and the keys are
	 * converted to String using {@link Object#toString()}.
	 *
	 * @param value
	 *            the value to convert
	 * @return the converted object or <code>null</code> if the value was <code>null</code> or the method could not
	 *         convert it.
	 */
	public static Object convertToJsonCompatibleValue(Object value) {
		Object result = null;
		if (value instanceof JsonRepresentable) {
			result = ((JsonRepresentable) value).toJSONObject();
		} else if (value instanceof Number || value instanceof String || value instanceof Boolean) {
			result = value;
		} else if (value instanceof JSONObject || value instanceof JSONArray) {
			result = value;
		} else if (value instanceof Date) {
			result = TypeConverterUtil.getConverter().convert(String.class, value);
		} else if (value instanceof Collection) {
			result = collectionToJsonArray(value);
		} else if (value instanceof Map) {
			result = mapToJsonObject(value);
		} else {
			result = tryUsingConverter(value);
		}

		return result;
	}

	private static Object tryUsingConverter(Object value) {
		Object result;
		// first check if the value could be converted to jsonObject directly
		result = TypeConverterUtil.getConverter().tryConvert(JSONObject.class, value);
		if (result == null) {
			// if no JSON object converter try to String
			result = TypeConverterUtil.getConverter().tryConvert(String.class, value);
		}
		return result;
	}

	private static Object collectionToJsonArray(Object value) {
		JSONArray array = new JSONArray();
		for (Object element : (Collection<?>) value) {
			Object converted = convertToJsonCompatibleValue(element);
			if (converted != null) {
				array.put(converted);
			}
		}
		return array;
	}

	private static Object mapToJsonObject(Object value) {
		JSONObject jsonObject = new JSONObject();
		for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
			Object converted = convertToJsonCompatibleValue(entry.getValue());
			if (converted != null) {
				addToJson(jsonObject, entry.getKey().toString(), converted);
			}
		}
		return jsonObject;
	}

	/**
	 * Copy JSON data to the given map by converting {@link JSONObject} to {@link Map} and {@link JSONArray} to
	 * {@link Collection}.
	 *
	 * @param <T>
	 *            the generic type
	 * @param json
	 *            the json to convert
	 * @return the build populated map from the given {@link JSONObject}
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> toMap(JSONObject json) {
		if (json == null) {
			return Collections.emptyMap();
		}
		Map<String, T> target = new HashMap<>();
		for (Iterator<String> iterator = json.keys(); iterator.hasNext();) {
			String key = iterator.next();
			Object object = JsonUtil.getValueOrNull(json, key);
			if (object instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject) object;
				target.put(key, (T) toMap(jsonObject));
			} else if (object instanceof JSONArray) {
				JSONArray array = (JSONArray) object;
				Collection<Object> collection = convertArrayToCollection(array);
				target.put(key, (T) collection);
			} else if (object != null) {
				target.put(key, (T) object);
			}
		}
		return target;
	}

	/**
	 * Convert array to collection. If element is {@link JSONObject} is converted to {@link Map} using the
	 * {@link #jsonToMap(JSONObject)} and {@link JSONArray} are converted to {@link Collection} via the current method.
	 *
	 * @param array
	 *            the array to convert
	 * @return the collection
	 */
	public static Collection<Object> convertArrayToCollection(JSONArray array) {
		if (array == null) {
			return Collections.emptyList();
		}
		Collection<Object> collection = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++) {
			Object item = array.opt(i);
			if (item instanceof JSONObject) {
				collection.add(toMap((JSONObject) item));
			} else if (item instanceof JSONArray) {
				Collection<Object> arrayToCollection = convertArrayToCollection((JSONArray) item);
				collection.add(arrayToCollection);
			} else if (item != null) {
				collection.add(item);
			}
		}
		return collection;
	}

	/**
	 * Builds a json by copying the properties returned by the given properties provider under the same keys. If the id
	 * parameter is non <code>null</code> it will be added to the json under <code>id</code> key.
	 *
	 * @param id
	 *            the id to add if non <code>null</code>
	 * @param propertiesProvider
	 *            the properties provider
	 * @param properties
	 *            properties to map.
	 * @return new json object containing the mapped properties or null if the provided instance is null.
	 */
	public static JSONObject buildFrom(Serializable id,
			Function<String, Serializable> propertiesProvider, String... properties) {
		JSONObject result = null;
		if (propertiesProvider != null && properties != null) {
			result = new JSONObject();
			for (String propertyName : properties) {
				addToJson(result, propertyName, propertiesProvider.apply(propertyName));
			}

			if (id != null) {
				addToJson(result, "id", id.toString());
			}
		}
		return result;
	}

	/**
	 * Builds a json by copying the properties returned by the given properties provider under names returned by the
	 * given mapping. The json properties names will be the value returned by the {@link StringPair#getFirst()} and the
	 * property name passed to the provider will be {@link StringPair#getSecond()}. If the id parameter is non
	 * <code>null</code> it will be added to the json under <code>id</code> key.
	 *
	 * @param id
	 *            the id to add if non <code>null</code>
	 * @param propertiesProvider
	 *            the properties provider
	 * @param properties
	 *            properties to map. The first element in the StringPair is the name of the json (destination) property,
	 *            the second element is the name of the source property.
	 * @return new json object containing the mapped properties or null if the provided instance is null.
	 */
	public static JSONObject buildFrom(Serializable id,
			Function<String, Serializable> propertiesProvider, StringPair... properties) {
		JSONObject result = null;
		if (propertiesProvider != null && properties != null) {
			result = new JSONObject();
			for (StringPair property : properties) {
				addToJson(result, property.getFirst(), propertiesProvider.apply(property.getSecond()));
			}

			if (id != null) {
				addToJson(result, "id", id.toString());
			}
		}
		return result;
	}

	/**
	 * Gets stream of json object keys.
	 *
	 * @param object
	 *            the object
	 * @return the key stream
	 */
	public static Stream<String> getKeyStream(JSONObject object) {
		return StreamSupport.stream(new SpliteratorIteratorProxyWithSize<>(object.keys(), object.length(),
				Spliterator.NONNULL | Spliterator.DISTINCT), false);
	}

	/**
	 * Provides a stream over the elements of the given {@link JSONArray}. The second argument is a function that
	 * fetches and converts an element from the array
	 *
	 * @param <T>
	 *            the generic type
	 * @param array
	 *            the array
	 * @param transformer
	 *            the transformer
	 * @return the stream
	 */
	public static <T> Stream<T> toStream(JSONArray array, IndexTransformer<JSONArray, T> transformer) {
		if (array == null) {
			return Stream.empty();
		}
		return StreamSupport.stream(new JsonArraySpliterator<>(array, transformer), false);
	}

	/**
	 * Provides a stream of {@link JSONObject}s from the given {@link JSONArray}. Note that if the elements of the array
	 * are not {@link JSONObject} a {@link ClassCastException} will be thrown.
	 *
	 * @param array
	 *            the source array
	 * @return the value stream
	 */
	public static Stream<JSONObject> toJsonObjectStream(JSONArray array) {
		return toStream(array, (index, arr) -> getFromArray(arr, index, JSONObject.class));
	}

	/**
	 * Provides a stream of {@link String}s from the given {@link JSONArray}. Note that if the elements of the array are
	 * not {@link String} a {@link ClassCastException} will be thrown.
	 *
	 * @param array
	 *            the source array
	 * @return the value stream
	 */
	public static Stream<String> toStringStream(JSONArray array) {
		return toStream(array, (index, arr) -> getFromArray(arr, index, String.class));
	}

	/**
	 * The Class JsonArraySpliterator.
	 *
	 * @param <T>
	 *            the generic type
	 */
	private static class JsonArraySpliterator<T> extends AbstractSpliterator<T> {
		private final JSONArray array;
		private IndexTransformer<JSONArray, T> transformer;
		private int index;

		/**
		 * Instantiates a new json array spliterator.
		 *
		 * @param array
		 *            the array
		 * @param transformer
		 *            the transformer
		 */
		public JsonArraySpliterator(JSONArray array, IndexTransformer<JSONArray, T> transformer) {
			super(array.length(), ORDERED | SIZED | NONNULL);
			this.array = array;
			this.transformer = Objects.requireNonNull(transformer, "Transformer function is required");
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (index >= array.length()) {
				return false;
			}
			T value = transformer.apply(index++, array);
			if (value != null) {
				action.accept(value);
				return true;
			}
			return false;
		}
	}

	/**
	 * {@link Spliterator} proxy of {@link Iterator} interface then the size is known but only means of accessing the
	 * elements is via {@link Iterator}.
	 * <p>
	 * Note that if the size is unknown better use {@link Spliterators#spliteratorUnknownSize(Iterator, int)}
	 *
	 * @author BBonev
	 * @param <T>
	 *            the generic type
	 */
	private static class SpliteratorIteratorProxyWithSize<T> extends AbstractSpliterator<T> {

		private final Iterator<T> itr;

		/**
		 * Instantiates a new spliterator iterator proxy with size.
		 *
		 * @param iterator
		 *            the iterator
		 * @param est
		 *            the est
		 * @param characteristics
		 *            the additional characteristics
		 */
		public SpliteratorIteratorProxyWithSize(Iterator<T> iterator, long est, int characteristics) {
			super(est, characteristics | Spliterator.SIZED);
			this.itr = iterator;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (itr.hasNext()) {
				action.accept(itr.next());
				return true;
			}
			return false;
		}

	}

}
