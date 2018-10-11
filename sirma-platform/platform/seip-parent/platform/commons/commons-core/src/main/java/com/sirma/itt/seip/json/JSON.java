package com.sirma.itt.seip.json;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Utility methods for {@link javax.json} JSON implementation
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/12/2017
 */
public class JSON {

	private JSON() {
		// utility class
	}

	/**
	 * Is the provided {@link JsonValue} of the provided type.
	 *
	 * @param v Value to check.
	 * @param expected Expected {@link JsonValue.ValueType}.
	 * @return {@code true} if the value is not {@code null} and it's type is the expected.
	 */
	public static boolean isType(JsonValue v, JsonValue.ValueType expected) {
		return v != null && v.getValueType() == expected;
	}

	/**
	 * Gets {@link JsonArray} form given {@link JsonObject} by key.
	 *
	 * @param key the key of the value that should be returned
	 * @param json the Json in which to search for the key
	 * @return {@link JsonArray} or null if the Json doesn't contain the given key
	 */
	public static JsonArray getArray(String key, JsonObject json) {
		if (json.containsKey(key)) {
			return json.getJsonArray(key);
		}
		return null;
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only of String elements and returns a collection with the
	 * contents of the array
	 *
	 * @param json the source json object to fetch the array from
	 * @param key the key that points to an array in the given json.
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 * not exists or the array was empty
	 */
	public static List<String> getStringArray(JsonObject json, String key) {
		JsonArray array = getArray(key, json);
		if (array == null) {
			return Collections.emptyList();
		}
		return getStringArray(array);
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only of String elements and returns a collection with the
	 * contents of the array.
	 *
	 * @param array the array to convert
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 * not exists or the array was empty
	 */
	public static List<String> getStringArray(JsonArray array) {
		if (array == null) {
			return Collections.emptyList();
		}
		return array.getValuesAs(JsonString.class).stream().map(JsonString::getString).collect(Collectors.toList());
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only {@link JsonObject}. The method's second argument is used
	 * for converting each element to other type.
	 *
	 * @param <E> the element type
	 * @param array the array to convert
	 * @param mapper the mapper to use for converting json objects to the expected types. The mapper chould return null.
	 * Null values will be filtered out.
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 * not exists or the array was empty
	 */
	public static <E> List<E> transformArray(JsonArray array, Function<JsonObject, E> mapper) {
		if (array == null) {
			return Collections.emptyList();
		}
		return array.getValuesAs(JsonObject.class).stream().map(mapper).filter(Objects::nonNull).collect(
				Collectors.toList());
	}

	/**
	 * Checks if the given {@link JsonValue} is {@code null} or empty.
	 *
	 * @param value Value to check.
	 * @return {@code true} if the structure is {@code null} or empty.
	 */
	public static boolean isBlank(JsonValue value) {
		if (value == null) {
			return true;
		}

		switch (value.getValueType()) {
			case NULL:
				return true;
			case ARRAY:
				return ((JsonArray) value).isEmpty();
			case OBJECT:
				return ((JsonObject) value).isEmpty();
			case STRING:
				return StringUtils.isBlank(((JsonString) value).getString());
			default:
				return false;
		}
	}

	/**
	 * Reads given input stream to {@link JsonStructure}, which is passed to the function. Then the function is executed
	 * with the produces json.
	 *
	 * @param <R> result type
	 * @param stream the request input stream
	 * @param function function executed, when the {@link JsonStructure} is created
	 * @return the result of the passed function or null if the json structure can't be parsed
	 */
	public static <R> R read(InputStream stream, Function<JsonStructure, R> function) {
		try (JsonReader reader = Json.createReader(stream)) {
			return function.apply(reader.read());
		}
	}

	/**
	 * Reads given input reader stream to {@link JsonStructure}, which is passed to the function. Then the function is
	 * executed with the produces json.
	 *
	 * @param <R> result type
	 * @param reader the source reader
	 * @param function function executed, when the {@link JsonStructure} is created
	 * @return the result of the passed function or null if the json structure can't be parsed
	 */
	public static <R> R read(Reader reader, Function<JsonStructure, R> function) {
		try (JsonReader jsonReader = Json.createReader(reader)) {
			return function.apply(jsonReader.read());
		}
	}

	/**
	 * Reads {@link JsonObject} from given request {@link InputStream}. Then the Json object is passed to the function,
	 * which is executed.
	 *
	 * @param <R> result type
	 * @param stream the request input stream
	 * @param function function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readObject(InputStream stream, Function<JsonObject, R> function) {
		try (JsonReader reader = Json.createReader(stream)) {
			return function.apply(reader.readObject());
		}
	}

	/**
	 * Reads given input {@link String} to {@link JsonStructure}, which is passed to the function. Then the function is
	 * executed with the produces json.
	 *
	 * @param <R> result type
	 * @param value the {@link String} which will be converted to {@link JsonObject}
	 * @param function function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function or null if the json can't be parsed
	 */
	public static <R> R readObject(String value, Function<JsonObject, R> function) {
		try (JsonReader reader = Json.createReader(new StringReader(value))) {
			return function.apply(reader.readObject());
		}
	}

	/**
	 * Reads {@link JsonArray} from given request {@link InputStream}. Then the Json array is passed to the function,
	 * which is executed.
	 *
	 * @param <R> result type
	 * @param stream the request input stream
	 * @param function function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readArray(InputStream stream, Function<JsonArray, R> function) {
		try (JsonReader reader = Json.createReader(stream)) {
			return function.apply(reader.readArray());
		}
	}

	/**
	 * Reads {@link JsonArray} from given request {@link String}. Then the Json array is passed to the function,
	 * which is executed.
	 *
	 * @param <R> result type
	 * @param input the {@link String} which will be converted to {@link JsonArray}
	 * @param function function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readArray(String input, Function<JsonArray, R> function) {
		try (JsonReader reader = Json.createReader(new StringReader(input))) {
			return function.apply(reader.readArray());
		}
	}

	/**
	 * Adds not null value to the passed {@link JsonGenerator}.
	 *
	 * @param generator the generator to which will be added
	 * @param key the key of the value
	 * @param value the value that will be checked and if not null will be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonGenerator generator, String key, String value) {
		return addIfNotNull(generator, key, value, null);
	}

	/**
	 * Adds the given value if non null or the default value if non null to the passed {@link JsonGenerator}.
	 *
	 * @param generator the generator to which will be added
	 * @param key the key of the value
	 * @param value the value that will be checked and if not null will be added
	 * @param defaultValue the default value if the value is null and this is non null it will be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonGenerator generator, String key, String value, String defaultValue) {
		if (StringUtils.isNotBlank(key)) {
			if (Objects.nonNull(value)) {
				generator.write(key, value);
				return true;
			} else if (Objects.nonNull(defaultValue)) {
				generator.write(key, defaultValue);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds given {@link Object} value to {@link JsonObjectBuilder}. The value is checked and cast, so it can be added
	 * to the builder. If the key is null or empty the value won't be added. If the value is null it won't be added.
	 *
	 * @param builder the builder to which will be added
	 * @param key the key for the value that will be added
	 * @param value the value that should be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonObjectBuilder builder, String key, Object value) {
		if (StringUtils.isBlank(key) || Objects.isNull(value)) {
			return false;
		}

		addTypeSafeValue(builder, key, value);
		return true;
	}

	@SuppressWarnings({ "unchecked", "squid:MethodCyclomaticComplexity" })
	private static void addTypeSafeValue(JsonObjectBuilder jsonBuilder, String key, Object value) {
		if (value instanceof String) {
			jsonBuilder.add(key, value.toString());
		} else if (value instanceof Number) {
			addToObjectIfNumber(jsonBuilder, key, value);
		} else if (value instanceof Boolean) {
			jsonBuilder.add(key, (Boolean) value);
		} else if (value instanceof Uri) {
			jsonBuilder.add(key, value.toString());
		} else if (value instanceof Date) {
			jsonBuilder.add(key, ISO8601DateFormat.format((Date) value));
		} else if (value instanceof JsonObject) {
			jsonBuilder.add(key, (JsonObject) value);
		} else if (value instanceof JsonArray) {
			jsonBuilder.add(key, (JsonArray) value);
		} else if (value instanceof Map) {
			jsonBuilder.add(key, convertToJsonObject((Map<String, ?>) value));
		} else if (value instanceof Collection) {
			jsonBuilder.add(key, convertToJsonArray((Collection<?>) value));
		} else {
			throw new IllegalArgumentException("Not implemented for: " + value.getClass());
		}
	}

	private static void addToObjectIfNumber(JsonObjectBuilder jsonBuilder, String key, Object value) {
		if (value instanceof Integer) {
			jsonBuilder.add(key, (Integer) value);
		} else if (value instanceof Long) {
			jsonBuilder.add(key, (Long) value);
		} else if (value instanceof Double) {
			jsonBuilder.add(key, (Double) value);
		} else if (value instanceof Float) {
			// its done this way, because if we work with float directly, it will be casted to double, which adds
			// additional precision to the number(adds more numbers after the floating point) and we don't want that
			jsonBuilder.add(key, Double.parseDouble(value.toString()));
		}
	}

	/**
	 * Converts map to {@link JsonObject}. If the passed map is null, empty object will be returned.
	 *
	 * @param map the map that should be converted
	 * @return {@link JsonObject}
	 */
	public static JsonObject convertToJsonObject(Map<String, ?> map) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		if (map == null) {
			return jsonBuilder.build();
		}

		for (Map.Entry<String, ?> entry : map.entrySet()) {
			addIfNotNull(jsonBuilder, entry.getKey(), entry.getValue());
		}

		return jsonBuilder.build();
	}

	/**
	 * Converts {@link Collection} to {@link JsonArray}. If the passed collection is null the method will return empty
	 * {@link JsonArray}. Null values are not processed.
	 *
	 * @param collection the collection that should be converted
	 * @return {@link JsonArray}
	 */
	@SuppressWarnings({ "unchecked", "squid:MethodCyclomaticComplexity" })
	public static JsonArray convertToJsonArray(Collection<?> collection) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		if (collection == null) {
			return arrayBuilder.build();
		}

		for (Object value : collection) {
			if (value == null) {
				continue;
			} else if (value instanceof String) {
				arrayBuilder.add(value.toString());
			} else if (value instanceof Number) {
				addToArrayIfNumber(arrayBuilder, value);
			} else if (value instanceof Boolean) {
				arrayBuilder.add((Boolean) value);
			} else if (value instanceof Date) {
				arrayBuilder.add(ISO8601DateFormat.format((Date) value));
			} else if (value instanceof Uri) {
				arrayBuilder.add(value.toString());
			} else if (value instanceof JsonObject) {
				arrayBuilder.add((JsonObject) value);
			} else if (value instanceof JsonArray) {
				arrayBuilder.add((JsonArray) value);
			} else if (value instanceof Map) {
				arrayBuilder.add(convertToJsonObject((Map<String, ?>) value));
			} else if (value instanceof Collection) {
				arrayBuilder.add(convertToJsonArray((Collection<?>) value));
			} else {
				throw new IllegalArgumentException("Not implemented for: " + value.getClass());
			}
		}

		return arrayBuilder.build();
	}

	private static void addToArrayIfNumber(JsonArrayBuilder arrayBuilder, Object value) {
		if (value instanceof Integer) {
			arrayBuilder.add((Integer) value);
		} else if (value instanceof Long) {
			arrayBuilder.add((Long) value);
		} else if (value instanceof Double) {
			arrayBuilder.add((Double) value);
		} else if (value instanceof Float) {
			// its done this way, because if we work with float directly, it will be casted to double, which adds
			// additional precision to the number(adds more numbers after the floating point) and we don't want that
			arrayBuilder.add(Double.parseDouble(value.toString()));
		}
	}

	/**
	 * Reads the {@link JsonObject} structure and converts it to plain map where the keys are the json keys and the
	 * values are extracted json values.
	 *
	 * @param json the json object to read
	 * @return the map containing all non null json properties or empty map if the json is empty or all elements were
	 * <code>null</code>
	 * @see #readJsonValue(JsonValue)
	 */
	public static Map<String, Serializable> jsonToMap(JsonObject json) {
		return toMap(json, false);
	}

	/**
	 * Reads the {@link JsonObject} structure and converts it to plain map where the keys are the json keys and the
	 * values are extracted json values.
	 *
	 * @param json object to read
	 * @param allowNullValues flag used to shows whether the method should filter {@code null} values, when building the
	 *        result map or not
	 * @return the map containing json properties or empty map if the passed json is {@code null} or empty
	 * @see #readJsonValue(JsonValue)
	 */
	public static Map<String, Serializable> toMap(JsonObject json, boolean allowNullValues) {
		if (json == null || json.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, Serializable> result = createHashMap(json.size());
		BiConsumer<String, JsonValue> consumer = (key, value) -> addNonNullValue(result, key, readJsonValue(value));
		if (allowNullValues) {
			consumer = (key, value) -> result.put(key, readJsonValue(value));
		}

		json.forEach(consumer);
		return result;
	}

	/**
	 * Reads the {@link JsonArray} structure and converts it to plain list where the values are extracted json values.
	 *
	 * @param json the json array to read
	 * @return the list containing all non null json values or empty list if the array was empty or all elements were
	 * <code>null</code>
	 * @see #readJsonValue(JsonValue)
	 */
	public static List<Serializable> jsonToList(JsonArray json) {
		if (json == null) {
			return Collections.emptyList();
		}
		List<Serializable> result = new ArrayList<>(json.size());
		for (JsonValue item : json) {
			addNonNullValue(result, readJsonValue(item));
		}
		return result;
	}

	/**
	 * Read json value object and extracts the value to plain java object. If the value is complex object or array then
	 * the methods {@link #jsonToMap(JsonObject)} or {@link #jsonToList(JsonArray)} will be used.
	 *
	 * @param value the value to read
	 * @return the serializable object that represent the {@link JsonValue} or <code>null</code> if the argument was
	 * null or represented {@link JsonValue#NULL}
	 */
	@SuppressWarnings("squid:MethodCyclomaticComplexity")
	public static Serializable readJsonValue(JsonValue value) {
		if (value == null) {
			return null;
		}
		switch (value.getValueType()) {
			case STRING:
				return ((JsonString) value).getString();
			case OBJECT:
				return (Serializable) jsonToMap((JsonObject) value);
			case ARRAY:
				return (Serializable) jsonToList((JsonArray) value);
			case FALSE:
				return Boolean.FALSE;
			case TRUE:
				return Boolean.TRUE;
			case NUMBER:
				JsonNumber number = (JsonNumber) value;
				return number.isIntegral() ? (Serializable) Long.valueOf(number.longValue())
						: (Serializable) Double.valueOf(number.doubleValue());
			default:
				return null;
		}
	}
}
