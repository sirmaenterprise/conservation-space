package com.sirma.itt.commons.utils.classes;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests {@link ClassUtils}
 * 
 * @author Adrian Mitev
 */
@Test
public class ClassUtilsTest {

	/**
	 * Tests instantiate() method.
	 */
	public void testInstantiate() {
		String newInstance = ClassUtils.instantiate("java.lang.String");
		Assert.assertNotNull(newInstance);
	}

}
