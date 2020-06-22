package com.sirma.itt.seip.collections;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.FixedSizeMap;

/**
 * Test for FixedSizeMap implementation
 *
 * @author BBonev
 */
@Test
public class FixedSizeMapTest {

	/**
	 * Test put method.
	 */
	public void testPutMethod() {
		Map<String, String> map = new FixedSizeMap<>(3);
		map.put("1", "1");
		map.put("2", "2");
		map.put("3", "3");
		map.put("4", "4");
		Assert.assertEquals(map.size(), 3);
		Assert.assertFalse(map.containsKey("1"));
	}

	/**
	 * Test put all with overflow.
	 */
	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Insufficient.*")
	public void testPutAllWithOverflow() {
		Map<String, String> fixed = new FixedSizeMap<>(3);
		Map<String, String> map = new HashMap<>();
		map.put("1", "1");
		map.put("2", "2");
		map.put("3", "3");
		map.put("4", "4");
		fixed.putAll(map);
	}

}
