package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;

/**
 * Tests for {@link DigestUtils}.
 *
 * @author Adrian Mitev
 */
@Test
public class DigestUtilsTest {

	/**
	 * Tests {@link DigestUtils#calculateDigest(Serializable)} with non-null value.
	 */
	public void testCalculateDigestForSerializable() {

		Serializable valueToConvert = "test123";

		TypeConverter mock = Mockito.mock(TypeConverter.class);
		Mockito.when(mock.convert(String.class, valueToConvert))
				.thenReturn((String) valueToConvert);
		try {
			Field declaredField = TypeConverterUtil.class.getDeclaredField("typeConverter");
			declaredField.setAccessible(true);
			declaredField.set(null, mock);
			declaredField.setAccessible(false);
		} catch (Exception e) {
			Assert.fail("TypeConverterUtil could not be initialized", e);
		}

		String result = DigestUtils.calculateDigest(valueToConvert);
		Assert.assertNotNull(result);
	}

	/**
	 * Tests {@link DigestUtils#calculateDigest(String)} with non-null value.
	 */
	public void testCalculateDigestForString() {
		Serializable valueToConvert = "test123";

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
}
