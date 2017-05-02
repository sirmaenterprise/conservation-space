package com.sirma.itt.seip.collections;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.CaseInsensitiveLinkedHashMap;

/**
 * Tests the custom {@link Map} implementation {@link CaseInsensitiveLinkedHashMap}.
 *
 * @author Mihail Radkov
 */
@Test
public class CaseInsensitiveLinkedHashMapTest {

	/**
	 * Tests the functionality behind {@link CaseInsensitiveLinkedHashMap#put(String, Object)}.
	 */
	public void testPuttingElements() {
		Map<String, String> map = new CaseInsensitiveLinkedHashMap<>();

		map.put("one", "one value");
		map.put("two", "two values");
		map.put("three", "three values");
		Assert.assertEquals(3, map.size(), "Map's size should be exactly 3");

		map.put("oNe", "one value");
		map.put("Two", "two values");
		map.put("THREE", "three values");
		Assert.assertEquals(3, map.size(), "Map's size should be exactly 3");

		Assert.assertNull(map.put(null, "some value"));

		String oldValue = map.put(null, "new value");
		Assert.assertEquals("some value", oldValue, "Value should be exactly 'some value'");
	}

	/**
	 * Tests the functionality behind {@link CaseInsensitiveLinkedHashMap#get(Object)}.
	 */
	public void testGettingElements() {
		Map<String, String> map = new CaseInsensitiveLinkedHashMap<>();

		map.put("one", "one value");
		String value = map.get("one");
		Assert.assertEquals("one value", value, "Value should be exactly 'one value'");

		value = map.get("ONe");
		Assert.assertEquals("one value", value, "Value should be exactly 'one value'");

		value = map.get("ONE");
		Assert.assertEquals("one value", value, "Value should be exactly 'one value'");

		map.put(null, "some value");
		value = map.get(null);
		Assert.assertEquals("some value", value, "Value should be exactly 'some value'");
	}

	/**
	 * Tests the functionality behind {@link CaseInsensitiveLinkedHashMap#get(Object)} when no object corresponds to the
	 * given key.
	 */
	public void testGettingMissingElement() {
		Map<String, String> map = new CaseInsensitiveLinkedHashMap<>();

		map.put("one", "one value");
		map.put("two", "two values");
		map.put("three", "three values");

		Assert.assertNull(map.get("missing"), "No element should be returned");
	}

	/**
	 * Tests the functionality behind {@link CaseInsensitiveLinkedHashMap#containsKey(Object)}.
	 */
	public void testContainingElements() {
		Map<String, String> map = new CaseInsensitiveLinkedHashMap<>();

		map.put("one", "one value");
		Assert.assertTrue(map.containsKey("one"), "Map should contain the key 'one'");
		Assert.assertTrue(map.containsKey("OnE"), "Map should contain the key 'OnE'");
		Assert.assertTrue(map.containsKey("ONE"), "Map should contain the key 'ONE'");
	}

	/**
	 * Tests the functionality behind {@link CaseInsensitiveLinkedHashMap#remove(Object)}.
	 */
	public void testRemoveElements() {
		Map<String, String> map = new CaseInsensitiveLinkedHashMap<>();

		map.put("one", "one value");
		Assert.assertNull(map.remove("missing"), "Map should have returned a null value");
		Assert.assertEquals("one value", map.remove("one"), "Map should have returned 'one value'");
		Assert.assertEquals(0, map.size(), "Map should be empty");

		map.put("two", "two values");
		Assert.assertEquals("two values", map.remove("TWo"), "Map should have returned 'two values'");

		map.put("three", "three values");
		Assert.assertEquals("three values", map.remove("THREE"), "Map should have returned 'three values'");
	}

}
