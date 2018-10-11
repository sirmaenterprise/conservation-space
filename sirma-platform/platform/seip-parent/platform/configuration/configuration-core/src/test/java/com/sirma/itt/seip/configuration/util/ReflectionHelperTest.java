/**
 *
 */
package com.sirma.itt.seip.configuration.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author BBonev
 */
public class ReflectionHelperTest {

	static String NAME1 = "config1";
	static String NAME2 = "config2";
	static String EMPTY = "";
	static Integer NUMBER = 0;
	static String NULL = "";
	String nonStatic = "config2";

	@Test
	public void test_getName() throws NoSuchFieldException, SecurityException {
		String string = ReflectionHelper.determineConfigName("", ReflectionHelperTest.class.getDeclaredField("NAME1"));
		assertEquals(string, NAME1);
	}

	@Test
	public void test_getName2() throws NoSuchFieldException, SecurityException {
		String string = ReflectionHelper.determineConfigName("test",
				ReflectionHelperTest.class.getDeclaredField("NAME2"));
		assertEquals(string, "test");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_getName_notStatic() throws NoSuchFieldException, SecurityException {
		ReflectionHelper.determineConfigName("", ReflectionHelperTest.class.getDeclaredField("nonStatic"));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_getName_empty() throws NoSuchFieldException, SecurityException {
		ReflectionHelper.determineConfigName("", ReflectionHelperTest.class.getDeclaredField("EMPTY"));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_getName_null() throws NoSuchFieldException, SecurityException {
		ReflectionHelper.determineConfigName("", ReflectionHelperTest.class.getDeclaredField("NULL"));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_getName_notString() throws NoSuchFieldException, SecurityException {
		ReflectionHelper.determineConfigName("", ReflectionHelperTest.class.getDeclaredField("NUMBER"));
	}
}
