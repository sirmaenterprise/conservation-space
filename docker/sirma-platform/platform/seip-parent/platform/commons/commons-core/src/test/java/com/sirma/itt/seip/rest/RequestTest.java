package com.sirma.itt.seip.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Tests for {@link Request}
 *
 * @author BBonev
 */
public class RequestTest {

	@Test
	public void noData() throws Exception {
		Request request = new Request();

		assertNull(request.getFirst("notExist"));
		assertEquals("default", request.getOrDefault("notExist", "default"));
		List<String> values = request.get("notExits");
		assertNotNull(values);
		assertTrue(values.isEmpty());
	}

	@Test
	public void getSingleValue() throws Exception {
		Map<String, List<String>> map = new HashMap<>();
		CollectionUtils.addValueToMap(map, "text", "value");
		CollectionUtils.addValueToMap(map, "int", "2");
		CollectionUtils.addValueToMap(map, "boolean", "true");
		CollectionUtils.addValueToMap(map, "empty", " ");
		Request request = new Request(map);

		assertEquals("value", request.getFirst("text"));
		assertEquals("2", request.getFirst("int"));
		assertEquals("true", request.getFirst("boolean"));

		assertEquals(Integer.valueOf(2), request.getFirstInteger("int"));
		assertTrue(request.getFirstBoolean("boolean"));
		assertEquals(Integer.valueOf(5), request.getIntegerOrDefault("text", Integer.valueOf(5)));
		assertEquals(Integer.valueOf(5), request.getIntegerOrDefault("empty", Integer.valueOf(5)));

		assertNull(request.getFirst("notExist"));
		assertEquals("default", request.getOrDefault("notExist", "default"));
		List<String> values = request.get("notExits");
		assertNotNull(values);
		assertTrue(values.isEmpty());
	}

	@Test
	public void getMultiple() throws Exception {
		Map<String, List<String>> map = new HashMap<>();
		CollectionUtils.addValueToMap(map, "int", "1");
		CollectionUtils.addValueToMap(map, "int", "2");
		CollectionUtils.addValueToMap(map, "int", "3");
		CollectionUtils.addValueToMap(map, "int", " ");
		CollectionUtils.addValueToMap(map, "int", "d");
		Request request = new Request(map);

		List<Integer> list = request.getIntegers("int");
		assertEquals(Arrays.asList(1, 2, 3), list);
	}
}
