package com.sirma.itt.idoc.web.util.sanitize;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link EmptyAttributeRemovePolicy}.
 * 
 * @author Adrian Mitev
 */
@Test
public class EmptyAttributeRemovePolicyTest {

	/**
	 * Tests the apply() method.
	 */
	public void testApply() {
		EmptyAttributeRemovePolicy policy = new EmptyAttributeRemovePolicy();
		String result = policy.apply("div", "style", "test");
		Assert.assertEquals("test", result);

		result = policy.apply("div", "style", "");
		Assert.assertNull(result);
	}

}
