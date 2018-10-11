package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.util.LoggingUtil;

/**
 * The Class LoggingUtilTest.
 */
public class LoggingUtilTest {

	/**
	 * Test shorten.
	 */
	@Test
	public void testShorten() {
		String message = "The quick brown fox jumps over the lazy dog";
		String shorten = LoggingUtil.shorten(message, 10);
		Assert.assertEquals("The quick ... 33 more", shorten);
		shorten = LoggingUtil.shorten(message, 50);
		Assert.assertEquals(message, shorten);

		Assert.assertNull(LoggingUtil.shorten(null, 10));
	}

	@Test
	public void testToStringMap() {
		Assert.assertNull(LoggingUtil.toString(null));

		Map<String, Serializable> map = new HashMap<>();
		map.put("key1", "shortValue");
		map.put("key2", "very long value");
		map.put("key3", null);

		String string = LoggingUtil.toString(map, 10);
		Assert.assertEquals("{key1=shortValue, key2=very long ... 5 more, key3=null}", string);
	}
}
