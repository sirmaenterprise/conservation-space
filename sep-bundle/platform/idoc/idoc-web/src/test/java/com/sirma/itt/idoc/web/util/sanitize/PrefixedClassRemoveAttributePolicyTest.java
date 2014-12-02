package com.sirma.itt.idoc.web.util.sanitize;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link PrefixedClassRemoveAttributePolicy}.
 * 
 * @author Adrian Mitev
 */
@Test
public class PrefixedClassRemoveAttributePolicyTest {

	/**
	 * Verifies that the apply() method removes only ng-* classes but not others.
	 */
	public void testRemoveOfClassesByPrefix() {
		PrefixedClassRemoveAttributePolicy policy = new PrefixedClassRemoveAttributePolicy("ng-");

		String result = policy.apply("test", "class", "ng-scope");
		Assert.assertTrue(result.isEmpty());

		result = policy.apply("test", "class", "test ng-scope");
		Assert.assertEquals(result, "test");

		result = policy.apply("test", "class", "");
		Assert.assertEquals(result, "");
	}

}
