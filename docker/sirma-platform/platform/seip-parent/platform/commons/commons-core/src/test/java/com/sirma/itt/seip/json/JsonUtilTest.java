/**
 *
 */
package com.sirma.itt.seip.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

/**
 * @author BBonev
 *
 */
public class JsonUtilTest {

	@Test
	public void test_addToJsonObject() {
		JsonUtil.addToJson(null, "somekey", "valueFornullKey");

		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, null, "valueFornullKey");

		JsonUtil.addToJson(object, "nullkey", null);
		assertFalse(object.has("nullkey"));

		JsonUtil.addToJson(object, "key", "value");
		assertTrue(object.has("key"));
	}

	@Test
	public void test_toStringStream() {
		JSONArray array = new JSONArray(Arrays.asList("1", "2", "3"));

		Stream<String> stream = JsonUtil.toStringStream(array);
		assertNotNull(stream);
		assertEquals(stream.count(), 3);
	}

	@Test
	public void test_getKeyStream() {
		JSONObject object = new JSONObject();

		JsonUtil.addToJson(object, "key1", "value1");
		JsonUtil.addToJson(object, "key2", "value2");
		JsonUtil.addToJson(object, "key3", "value3");

		Stream<String> stream = JsonUtil.getKeyStream(object);
		assertNotNull(stream);

		assertTrue(stream.allMatch(k -> k.startsWith("key")));
	}

	@Test
	public void test_convertArrayToCollection() {
		JSONArray array = new JSONArray();
		JsonUtil.addToJson(array, "1");

		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "key", "value");
		JsonUtil.addToJson(array, jsonObject);

		JSONArray subArray = new JSONArray(Arrays.asList("2", "3"));

		JsonUtil.addToJson(array, subArray);

		Collection<Object> collection = JsonUtil.convertArrayToCollection(array);

		assertNotNull(collection);
		assertFalse(collection.isEmpty());

		assertEquals(collection.size(), 3);

		Object[] objects = collection.toArray();
		assertEquals(objects[0], "1");
		assertEquals(objects[1], Collections.singletonMap("key", "value"));
		assertEquals(objects[2], Arrays.asList("2", "3"));
	}

	@Test
	public void test_toMap() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "single", "value");
		JsonUtil.addToJson(object, "complex", new JSONObject(Collections.singletonMap("key", "mapValue")));
		JsonUtil.addToJson(object, "array", new JSONArray(Arrays.asList("el1", "el2")));

		Map<String, Object> map = JsonUtil.toMap(object);

		assertNotNull(map);
		assertFalse(map.isEmpty());

		assertEquals(map.get("single"), "value");
		assertEquals(map.get("complex"), Collections.singletonMap("key", "mapValue"));
		assertEquals(map.get("array"), Arrays.asList("el1", "el2"));
	}

}
