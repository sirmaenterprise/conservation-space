package com.sirma.itt.emf.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.LoggingUtil;

/**
 * The Class LoggingUtilTest.
 */
@Test
public class LoggingUtilTest {

	/**
	 * Test shorten.
	 */
	public void testShorten() {
		String message = "The quick brown fox jumps over the lazy dog";
		String shorten = LoggingUtil.shorten(message, 10);
		Assert.assertEquals(shorten, "The quick ... 33 more");
		shorten = LoggingUtil.shorten(message, 50);
		Assert.assertEquals(shorten, message);
	}
}
