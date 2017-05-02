package com.sirma.itt.seip.rest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParsingException;

import org.junit.Test;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Test for {@link JSON}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings({ "static-method", "boxing" })
public class JSONTest {

	private static final SimpleDateFormat TEST_DATE_FORMAT = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

	private static final String TEST_DATE = "19-02-1976 00:00:00";

	private static final JsonObject EMPTY_JSON_OBJECT = Json.createObjectBuilder().build();

	private static final String TYPES_JSON = "json-util-types-test.json";

	private static final String TYPES_CONVERT_COLLECTION_JSON = "json-util-convert-collection-test.json";

	@Test
	public void getArray_emptyJson_null() {
		JsonObject json = Json.createObjectBuilder().build();
		assertNull(JSON.getArray("key", json));
	}

	@Test
	public void getArray() {
		JsonArray array = Json.createArrayBuilder().add("value").build();
		JsonObject json = Json.createObjectBuilder().add("key", array).build();
		JsonArray result = JSON.getArray("key", json);
		assertNotNull(result);
		assertTrue(!result.isEmpty());
	}

	@Test(expected = ClassCastException.class)
	public void getArray_notArray() {
		JsonObject json = Json.createObjectBuilder().add("key", "").build();
		JSON.getArray("key", json);
	}

	@Test
	public void readFromStream() {
		String json = "{\"number\":7}";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		int result = JSON.read(is, (v) -> {
			return ((JsonObject) v).getInt("number") * 2;
		});

		assertEquals(14, result);
	}

	@Test
	public void read_streamArray() {
		String json = "[7,8]";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		int result = JSON.read(is, (v) -> {
			return ((JsonArray) v).getInt(0) + ((JsonArray) v).getInt(1);
		});

		assertEquals(15, result);
	}

	@Test
	public void readFromReader() {
		String json = "{\"number\":7}";
		int result = JSON.read(new StringReader(json), (v) -> {
			return ((JsonObject) v).getInt("number") * 2;
		});

		assertEquals(14, result);
	}

	@Test
	public void read_ReaderArray() {
		String json = "[7,8]";
		int result = JSON.read(new StringReader(json), (v) -> {
			return ((JsonArray) v).getInt(0) + ((JsonArray) v).getInt(1);
		});

		assertEquals(15, result);
	}

	@Test(expected = NullPointerException.class)
	public void read_nullStream_exception() {
		JSON.read((InputStream) null, (v) -> "");
	}

	@Test(expected = NullPointerException.class)
	public void read_nullReader_exception() {
		JSON.read((Reader) null, (v) -> "");
	}

	@Test(expected = JsonParsingException.class)
	public void read_streamProblem_exception() throws IOException {
		try (InputStream is = mock(InputStream.class)) {
			when(is.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			JSON.read(is, (v) -> "");
		}
	}

	@Test
	public void readObject() {
		String json = "{\"number\":2}";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		int result = JSON.readObject(is, (v) -> {
			return v.getInt("number") * 2;
		});

		assertEquals(4, result);
	}

	@Test(expected = NullPointerException.class)
	public void readObject_nullStream_exception() {
		InputStream stream = null;
		JSON.readObject(stream, (v) -> "");
	}

	@Test(expected = JsonParsingException.class)
	public void readObject_streamProblem_exception() throws IOException {
		try (InputStream is = mock(InputStream.class)) {
			when(is.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			JSON.readObject(is, (v) -> "");
		}
	}

	@Test
	public void readObjectString() {
		int result = JSON.readObject("{\"number\":2}", (v) -> {
			return v.getInt("number") * 2;
		});

		assertEquals(4, result);
	}

	@Test(expected = NullPointerException.class)
	public void readObjectString_nullStream_exception() {
		String value = null;
		JSON.readObject(value, (v) -> "");
	}

	@Test(expected = JsonException.class)
	public void readObjectString_notObject_exception() {
		JSON.readObject("[]", (v) -> "");
	}

	@Test(expected = JsonException.class)
	public void readObjectString_notJsonFormat_exception() {
		JSON.readObject("{'id' : value : []}", (v) -> "");
	}

	@Test
	public void readArray() {
		String json = "[2,5]";
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
		int result = JSON.readArray(is, (v) -> {
			return v.getInt(0) + v.getInt(1);
		});

		assertEquals(7, result);
	}

	@Test(expected = NullPointerException.class)
	public void readArray_nullStream_exception() {
		JSON.readArray(null, (v) -> "");
	}

	@Test(expected = JsonParsingException.class)
	public void readArray_streamProblem_exception() throws IOException {
		try (InputStream is = mock(InputStream.class)) {
			when(is.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
			JSON.readArray(is, (v) -> "");
		}
	}

	@Test
	public void addIfNotNullGenerator_nullKey() {
		addIfNotNullGeneratorInternal(null, "value");
	}

	@Test
	public void addIfNotNullGenerator_emptyKey() {
		addIfNotNullGeneratorInternal("", "value");
	}

	@Test
	public void addIfNotNullGenerator_nullValue() {
		addIfNotNullGeneratorInternal("key", null);
	}

	private static void addIfNotNullGeneratorInternal(String key, String value) {
		try (JsonGenerator generator = mock(JsonGenerator.class)) {
			JSON.addIfNotNull(generator, key, value);
			verify(generator, never()).write(key, value);
		}
	}

	@Test
	public void addIfNotNullGenerator_notNullValueEmpty() {
		addIfNotNullGeneratorWithValueInternal("");
	}

	@Test
	public void addIfNotNullGenerator_notNullValueNotEmpty() {
		addIfNotNullGeneratorWithValueInternal("value");
	}

	@Test
	public void addIfNotNullGenerator_NullValueNotNullDefaultValue() {
		try (JsonGenerator generator = mock(JsonGenerator.class)) {
			JSON.addIfNotNull(generator, "key", null, "defaultValue");
			verify(generator).write("key", "defaultValue");
		}
	}

	private static void addIfNotNullGeneratorWithValueInternal(String value) {
		try (JsonGenerator generator = mock(JsonGenerator.class)) {
			JSON.addIfNotNull(generator, "key", value);
			verify(generator).write("key", value);
		}
	}

	@Test
	public void addIfNotNullBuilder_nullKey() {
		addIfNotNullBuilderInternal(null, "value");
	}

	@Test
	public void addIfNotNullBuilder_emptyKey() {
		addIfNotNullBuilderInternal("", "value");
	}

	@Test
	public void addIfNotNullBuilder_nullValue() {
		addIfNotNullBuilderInternal("key", null);
	}

	private static void addIfNotNullBuilderInternal(String key, String value) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JSON.addIfNotNull(builder, key, value);
		assertEquals(EMPTY_JSON_OBJECT, builder.build());
	}

	@Test
	public void addIfNotNullBuilder_notNullValueEmpty() {
		addIfNotNullBuilderWithValueInternal("");
	}

	@Test
	public void addIfNotNullBuilder_notNullValueNotEmpty() {
		addIfNotNullBuilderWithValueInternal("value");
	}

	private static void addIfNotNullBuilderWithValueInternal(String value) {
		JsonObject expected = Json.createObjectBuilder().add("key", value).build();
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JSON.addIfNotNull(builder, "key", value);
		assertEquals(expected, builder.build());
	}

	@Test
	public void addIfNotNullBuilder_types() throws ParseException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JSON.addIfNotNull(builder, "string", "string");
		JSON.addIfNotNull(builder, "int", 1);
		JSON.addIfNotNull(builder, "double", 1.1);
		JSON.addIfNotNull(builder, "Long", 2L);
		JSON.addIfNotNull(builder, "boolean", true);
		HashMap<String, Object> map = new HashMap<>();
		map.put("mapKey", "map-value");
		JSON.addIfNotNull(builder, "map", map);
		Collection<String> collection = Arrays.asList("collection-value");
		JSON.addIfNotNull(builder, "collection", collection);
		JSON.addIfNotNull(builder, "jsonObject", Json.createObjectBuilder().build());
		JSON.addIfNotNull(builder, "jsonArray", Json.createArrayBuilder().build());
		JSON.addIfNotNull(builder, "uri", new ShortUri("short-uri"));
		JSON.addIfNotNull(builder, "date", TEST_DATE_FORMAT.parse(TEST_DATE));
		JSON.addIfNotNull(builder, "float", 1.5f);

		JsonObject expected = Json
				.createReader(getClass().getClassLoader().getResourceAsStream(TYPES_JSON))
					.readObject();
		assertEquals(expected, builder.build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void addIfNotNullBuilder_illegalType() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JSON.addIfNotNull(builder, "string", new JSONTest());
	}

	@Test
	public void convertToJsonArray_types() throws ParseException {
		JsonArray expected = Json
				.createReader(getClass().getClassLoader().getResourceAsStream(TYPES_CONVERT_COLLECTION_JSON))
					.readArray();
		Collection<?> collection = Arrays.asList("string", 2, 3.0, 4L, true, new HashMap<>(), new ArrayList<>(), null,
				1.6f, new ShortUri("short-uri"), TEST_DATE_FORMAT.parse(TEST_DATE));
		JsonArray array = JSON.convertToJsonArray(collection);
		assertEquals(expected, array);
	}

	@Test(expected = IllegalArgumentException.class)
	public void convertToJsonArray_illegalType() {
		JSON.convertToJsonArray(Arrays.asList(new JSONTest()));
	}

	@Test
	public void convertToJsonArray_nullCollection() {
		JsonArray convertToJsonArray = JSON.convertToJsonArray(null);
		assertEquals(0, convertToJsonArray.size());
	}

	@Test
	public void convertToJsonObject_nullCollection() {
		JsonObject convertToJsonObject = JSON.convertToJsonObject(null);
		assertEquals(EMPTY_JSON_OBJECT, convertToJsonObject);
	}

	@Test
	public void readJsonValue() throws Exception {
		Map<String, Serializable> map = JSON.readObject(getClass().getClassLoader().getResourceAsStream(TYPES_JSON),
				JSON::jsonToMap);
		assertNotNull(map);
		assertEquals("string", map.get("string"));
		assertEquals(1L, map.get("int"));
		assertEquals(Double.valueOf(1.1), map.get("double"));
		assertEquals(2L, map.get("Long"));
		assertEquals(Boolean.TRUE, map.get("boolean"));
		assertEquals(Collections.singletonMap("mapKey", "map-value"), map.get("map"));
		assertEquals(Arrays.asList("collection-value"), map.get("collection"));
		assertTrue(map.containsKey("jsonObject"));
		assertTrue(map.containsKey("jsonArray"));
	}

	@Test
	public void readJsonValue_nulls() throws Exception {
		assertNull(JSON.readJsonValue(null));
		assertNull(JSON.readJsonValue(JsonValue.NULL));
	}

	@Test
	public void getDateRange_nullJson() {
		assertNull(JSON.getDateRange(null));
	}

	@Test
	public void getDateRange_emptyJson() {
		assertNull(JSON.getDateRange(EMPTY_JSON_OBJECT));
	}

	@Test
	public void getDateRange_emptyDateRangeJson() {
		JsonObject emptyDateRangeJson = Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, EMPTY_JSON_OBJECT).build();
		assertNull(JSON.getDateRange(emptyDateRangeJson));
	}

	@Test
	public void getDateRange_withEmptyStartDate() {
		DateRange dateRange = JSON.getDateRange(buildDateRangeTestJson("", ISO8601DateFormat.format(new Date())));
		assertNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_withEmptyEndDate() {
		DateRange dateRange = JSON.getDateRange(buildDateRangeTestJson(ISO8601DateFormat.format(new Date()), ""));
		assertNotNull(dateRange.getFirst());
		assertNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_withBothEmpty() {
		assertNull(JSON.getDateRange(buildDateRangeTestJson("", "")));
	}

	@Test
	public void getDateRange_withOneDate() {
		JsonObjectBuilder dates = Json.createObjectBuilder().add(JsonKeys.END, ISO8601DateFormat.format(new Date()));
		JsonObject datesObject = Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, dates).build();
		DateRange dateRange = JSON.getDateRange(datesObject);
		assertNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	@Test
	public void getDateRange_dateRangeWithBothDates() {
		DateRange dateRange = JSON.getDateRange(
				buildDateRangeTestJson(ISO8601DateFormat.format(new Date()), ISO8601DateFormat.format(new Date())));
		assertNotNull(dateRange.getFirst());
		assertNotNull(dateRange.getSecond());
	}

	private static JsonObject buildDateRangeTestJson(String start, String end) {
		JsonObjectBuilder dates = Json.createObjectBuilder().add(JsonKeys.START, start).add(JsonKeys.END, end);
		return Json.createObjectBuilder().add(JsonKeys.DATE_RANGE, dates).build();
	}

}
