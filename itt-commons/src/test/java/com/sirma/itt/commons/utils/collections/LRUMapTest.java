package com.sirma.itt.commons.utils.collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

/**
 * Tests for LRUMap
 * 
 * @author Adrian Mitev
 */
@Test
public class LRUMapTest {

	/**
	 * Tests if the map applies correctly the LRU algorithm.
	 */
	public void testLRU() {
		LRUMap<String, String> lruMap = new LRUMap<String, String>(5);

		// add 5 strings
		lruMap.put("1", "1");
		lruMap.put("2", "2");
		lruMap.put("3", "3");
		lruMap.put("4", "4");
		lruMap.put("5", "5");

		assertEquals(lruMap.size(), 5);

		// add another string and verify that the size is still 5 - max limit

		lruMap.put("6", "6");
		assertEquals(lruMap.size(), 5);

		// verify that the eldest element is removed ("1" string)
		assertFalse(lruMap.containsKey("1"));
	}

}
