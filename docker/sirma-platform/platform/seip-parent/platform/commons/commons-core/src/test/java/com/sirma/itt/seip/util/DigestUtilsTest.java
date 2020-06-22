package com.sirma.itt.seip.util;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link DigestUtils}.
 *
 * @author Adrian Mitev
 */
@Test
public class DigestUtilsTest {

	/**
	 * Tests {@link DigestUtils#calculateDigest(String)} with non-null value.
	 */
	public void testCalculateDigestForString() {
		String valueToConvert = "test123";

		String result = DigestUtils.calculateDigest(valueToConvert);
		Assert.assertNotNull(result);
	}

	/**
	 * Tests {@link DigestUtils#calculateDigest(Serializable)} with null value.
	 */
	public void testCalculateDigestForStringWithNullValue() {
		String result = DigestUtils.calculateDigest(null);
		Assert.assertNull(result);
	}

	public void testTruncateWithDigest() {
		assertEquals(DigestUtils
				.truncateWithDigest(
						"12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
						100)
					.length(),
				100);

		assertEquals(DigestUtils.truncateWithDigest("123456789", 5).length(), 5);
	}
}
