package com.sirma.itt.seip.rest.utils;

import static com.sirma.itt.seip.rest.utils.JsonKeys.DATE_RANGE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.END;
import static com.sirma.itt.seip.rest.utils.JsonKeys.START;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * JSON utility methods.
 *
 * @author yasko
 */
public final class JSON {

	private JSON() {
		// utility
	}

	/**
	 * Is the provided {@link JsonValue} of the provided type.
	 *
	 * @param v
	 *            Value to check.
	 * @param expected
	 *            Expected {@link JsonValue.ValueType}.
	 * @return {@code true} if the value is not {@code null} and it's type is the expected.
	 */
	public static boolean isType(JsonValue v, JsonValue.ValueType expected) {
		return com.sirma.itt.seip.json.JSON.isType(v, expected);
	}

	/**
	 * Gets {@link JsonArray} form given {@link JsonObject} by key.
	 *
	 * @param key
	 *            the key of the value that should be returned
	 * @param json
	 *            the Json in which to search for the key
	 * @return {@link JsonArray} or null if the Json doesn't contain the given key
	 */
	public static JsonArray getArray(String key, JsonObject json) {
		return com.sirma.itt.seip.json.JSON.getArray(key,json);
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only of String elements and returns a collection with the
	 * contents of the array
	 *
	 * @param json
	 *            the source json object to fetch the array from
	 * @param key
	 *            the key that points to an array in the given json.
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 *         not exists or the array was empty
	 */
	public static List<String> getStringArray(JsonObject json, String key) {
		return com.sirma.itt.seip.json.JSON.getStringArray(json, key);
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only of String elements and returns a collection with the
	 * contents of the array.
	 *
	 * @param array
	 *            the array to convert
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 *         not exists or the array was empty
	 */
	public static List<String> getStringArray(JsonArray array) {
		return com.sirma.itt.seip.json.JSON.getStringArray(array);
	}

	/**
	 * Gets the data from {@link JsonArray} that consists only {@link JsonObject}. The method's second argument is used
	 * for converting each element to other type.
	 *
	 * @param <E>
	 *            the element type
	 * @param array
	 *            the array to convert
	 * @param mapper
	 *            the mapper to use for converting json objects to the expected types. The mapper chould return null.
	 *            Null values will be filtered out.
	 * @return the list that contains the elements of the array found in the given json or empty list if the array does
	 *         not exists or the array was empty
	 */
	public static <E> List<E> transformArray(JsonArray array, Function<JsonObject, E> mapper) {
		return com.sirma.itt.seip.json.JSON.transformArray(array, mapper);
	}

	/**
	 * Checks if the given {@link JsonValue} is {@code null} or empty.
	 *
	 * @param value
	 *            Value to check.
	 * @return {@code true} if the structure is {@code null} or empty.
	 */
	public static boolean isBlank(JsonValue value) {
		return com.sirma.itt.seip.json.JSON.isBlank(value);
	}

	/**
	 * Reads given input stream to {@link JsonStructure}, which is passed to the function. Then the function is executed
	 * with the produces json.
	 *
	 * @param <R>
	 *            result type
	 * @param stream
	 *            the request input stream
	 * @param function
	 *            function executed, when the {@link JsonStructure} is created
	 * @return the result of the passed function or null if the json structure can't be parsed
	 */
	public static <R> R read(InputStream stream, Function<JsonStructure, R> function) {
		return com.sirma.itt.seip.json.JSON.read(stream, function);
	}

	/**
	 * Reads given input reader stream to {@link JsonStructure}, which is passed to the function. Then the function is
	 * executed with the produces json.
	 *
	 * @param <R>
	 *            result type
	 * @param reader
	 *            the source reader
	 * @param function
	 *            function executed, when the {@link JsonStructure} is created
	 * @return the result of the passed function or null if the json structure can't be parsed
	 */
	public static <R> R read(Reader reader, Function<JsonStructure, R> function) {
		return com.sirma.itt.seip.json.JSON.read(reader, function);
	}

	/**
	 * Reads {@link JsonObject} from given request {@link InputStream}. Then the Json object is passed to the function,
	 * which is executed.
	 *
	 * @param <R>
	 *            result type
	 * @param stream
	 *            the request input stream
	 * @param function
	 *            function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readObject(InputStream stream, Function<JsonObject, R> function) {
		return com.sirma.itt.seip.json.JSON.readObject(stream, function);
	}

	/**
	 * Reads given input {@link String} to {@link JsonStructure}, which is passed to the function. Then the function is
	 * executed with the produces json.
	 *
	 * @param <R>
	 *            result type
	 * @param value
	 *            the {@link String} which will be converted to {@link JsonObject}
	 * @param function
	 *            function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function or null if the json can't be parsed
	 */
	public static <R> R readObject(String value, Function<JsonObject, R> function) {
		return com.sirma.itt.seip.json.JSON.readObject(value, function);
	}

	/**
	 * Reads {@link JsonArray} from given request {@link InputStream}. Then the Json array is passed to the function,
	 * which is executed.
	 *
	 * @param <R>
	 *            result type
	 * @param stream
	 *            the request input stream
	 * @param function
	 *            function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readArray(InputStream stream, Function<JsonArray, R> function) {
		return com.sirma.itt.seip.json.JSON.readArray(stream, function);
	}

	/**
	 * Reads {@link JsonArray} from given request {@link String}. Then the Json array is passed to the function,
	 * which is executed.
	 *
	 * @param <R>
	 *            result type
	 * @param input
	 *            the {@link String} which will be converted to {@link JsonArray}
	 * @param function
	 *            function executed, when the {@link JsonObject} is created
	 * @return the result of the passed function
	 */
	public static <R> R readArray(String input, Function<JsonArray, R> function) {
		return com.sirma.itt.seip.json.JSON.readArray(input, function);
	}

	/**
	 * Adds not null value to the passed {@link JsonGenerator}.
	 *
	 * @param generator
	 *            the generator to which will be added
	 * @param key
	 *            the key of the value
	 * @param value
	 *            the value that will be checked and if not null will be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonGenerator generator, String key, String value) {
		return com.sirma.itt.seip.json.JSON.addIfNotNull(generator, key, value);
	}

	/**
	 * Adds the given value if non null or the default value if non null to the passed {@link JsonGenerator}.
	 *
	 * @param generator
	 *            the generator to which will be added
	 * @param key
	 *            the key of the value
	 * @param value
	 *            the value that will be checked and if not null will be added
	 * @param defaultValue
	 *            the default value if the value is null and this is non null it will be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonGenerator generator, String key, String value, String defaultValue) {
		return com.sirma.itt.seip.json.JSON.addIfNotNull(generator, key, value, defaultValue);
	}

	/**
	 * Adds given {@link Object} value to {@link JsonObjectBuilder}. The value is checked and cast, so it can be added
	 * to the builder. If the key is null or empty the value won't be added. If the value is null it won't be added.
	 *
	 * @param builder
	 *            the builder to which will be added
	 * @param key
	 *            the key for the value that will be added
	 * @param value
	 *            the value that should be added
	 * @return true, if successful
	 */
	public static boolean addIfNotNull(JsonObjectBuilder builder, String key, Object value) {
		return com.sirma.itt.seip.json.JSON.addIfNotNull(builder, key, value);
	}

	/**
	 * Converts map to {@link JsonObject}. If the passed map is null, empty object will be returned.
	 *
	 * @param map
	 *            the map that should be converted
	 * @return {@link JsonObject}
	 */
	public static JsonObject convertToJsonObject(Map<String, ?> map) {
		return com.sirma.itt.seip.json.JSON.convertToJsonObject(map);
	}

	/**
	 * Converts {@link Collection} to {@link JsonArray}. If the passed collection is null the method will return empty
	 * {@link JsonArray}. Null values are not processed.
	 *
	 * @param collection
	 *            the collection that should be converted
	 * @return {@link JsonArray}
	 */
	public static JsonArray convertToJsonArray(Collection<?> collection) {
		return com.sirma.itt.seip.json.JSON.convertToJsonArray(collection);
	}

	/**
	 * Reads the {@link JsonObject} structure and converts it to plain map where the keys are the json keys and the
	 * values are extracted json values.
	 *
	 * @param json
	 *            the json object to read
	 * @return the map containing all non null json properties or empty map if the json is empty or all elements were
	 *         <code>null</code>
	 * @see #readJsonValue(JsonValue)
	 */
	public static Map<String, Serializable> jsonToMap(JsonObject json) {
		return com.sirma.itt.seip.json.JSON.jsonToMap(json);
	}

	/**
	 * Reads the {@link JsonArray} structure and converts it to plain list where the values are extracted json values.
	 *
	 * @param json
	 *            the json array to read
	 * @return the list containing all non null json values or empty list if the array was empty or all elements were
	 *         <code>null</code>
	 * @see #readJsonValue(JsonValue)
	 */
	public static List<Serializable> jsonToList(JsonArray json) {
		return com.sirma.itt.seip.json.JSON.jsonToList(json);
	}

	/**
	 * Read json value object and extracts the value to plain java object. If the value is complex object or array then
	 * the methods {@link #jsonToMap(JsonObject)} or {@link #jsonToList(JsonArray)} will be used.
	 *
	 * @param value
	 *            the value to read
	 * @return the serializable object that represent the {@link JsonValue} or <code>null</code> if the argument was
	 *         null or represented {@link JsonValue#NULL}
	 */
	public static Serializable readJsonValue(JsonValue value) {
		return com.sirma.itt.seip.json.JSON.readJsonValue(value);
	}

	/**
	 * Builds {@link DateRange} object from the passed {@link JsonObject}. The json should contain {@link JsonObject}
	 * mapped to key {@link JsonKeys#DATE_RANGE}. This object should contain one of the two or both properties -
	 * {@link JsonKeys#START}, {@link JsonKeys#END}. If one of the properties is missing or its value is empty, its
	 * value will be set to <code>null</code> in the build {@link DateRange} object.
	 * <p>
	 * NOTE - The dates should be in the proper ISO format.
	 *
	 * @param json
	 *            used to retrieved the date properties. Should contain {@link JsonKeys#DATE_RANGE} object which should
	 *            contain at least {@link JsonKeys#START} with value or {@link JsonKeys#END} with value
	 * @return {@link DateRange} object
	 */
	public static DateRange getDateRange(JsonObject json) {
		if (json == null || !json.containsKey(DATE_RANGE)) {
			return null;
		}

		JsonObject dateRangeJson = json.getJsonObject(DATE_RANGE);
		if (!dateRangeJson.containsKey(START) && !dateRangeJson.containsKey(END)) {
			return null;
		}

		String firstDate = dateRangeJson.getString(START, null);
		String secondDate = dateRangeJson.getString(END, null);
		if (StringUtils.isBlank(firstDate) && StringUtils.isBlank(secondDate)) {
			return null;
		}

		return new DateRange(ISO8601DateFormat.parse(firstDate), ISO8601DateFormat.parse(secondDate));
	}
}