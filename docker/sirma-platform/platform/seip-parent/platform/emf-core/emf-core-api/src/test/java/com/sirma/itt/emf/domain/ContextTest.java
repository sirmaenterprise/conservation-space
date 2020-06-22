package com.sirma.itt.emf.domain;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.context.Context;

/**
 * The Class ContextTest.
 *
 * @author BBonev
 */
@Test
public class ContextTest {

	/** The Constant KEY. */
	private static final String KEY = "key";

	/**
	 * Test basic operations.
	 */
	public void testBasicOperations() {
		Context<String, Object> context = new Context<String, Object>();
		context.put(KEY, "1");

		Assert.assertTrue(context.containsKey(KEY));
		Assert.assertFalse(context.isEmpty());
		Assert.assertEquals(context.get(KEY), "1");
		Assert.assertTrue(context.containsValue("1"));
		Assert.assertEquals(context.size(), 1);
		Assert.assertEquals(context.remove(KEY), "1");
	}

	/**
	 * Test same value.
	 */
	public void testSameValue() {
		Context<String, Object> context = new Context<String, Object>();
		context.put(KEY, "1");

		Assert.assertEquals(context.getIfSameType(KEY, String.class), "1");
		Assert.assertEquals(context.getIfSameType(KEY, String.class, "2"), "1");
		Assert.assertEquals(context.getIfSameType(KEY, Integer.class, 2), Integer.valueOf(2));
	}

	/**
	 * Test equals.
	 */
	public void testEquals() {
		Context<String, Object> context1 = new Context<String, Object>();
		context1.put(KEY, "1");
		Context<String, Object> context2 = new Context<String, Object>();
		context2.put(KEY, "1");

		Assert.assertTrue(context1.equals(context2));

		context1.put("key2", 2);
		Assert.assertFalse(context1.equals(context2));

	}

}
