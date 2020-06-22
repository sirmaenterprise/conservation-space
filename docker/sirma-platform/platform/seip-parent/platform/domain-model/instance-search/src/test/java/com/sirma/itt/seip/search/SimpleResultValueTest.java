package com.sirma.itt.seip.search;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for the default implementation of {@link ResultValue}'s {@link com.sirma.itt.seip.search.ResultValue.SimpleResultValue}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/07/2017
 */
public class SimpleResultValueTest {

	@Test
	public void equals_shouldConsiderNameAndValue() throws Exception {
		ResultValue value1 = ResultValue.create("key", "value");
		ResultValue value2 = ResultValue.create("key", "value");
		assertEquals(value1, value2);

		value1 = ResultValue.create("key", "value1");
		value2 = ResultValue.create("key", "value2");
		assertNotEquals(value1, value2);

		value1 = ResultValue.create("key1", "value");
		value2 = ResultValue.create("key2", "value");
		assertNotEquals(value1, value2);


		value1 = ResultValue.create("key", "value");
		value2 = ResultValue.create("key", null);
		assertNotEquals(value1, value2);


		value1 = ResultValue.create(null, "value");
		value2 = ResultValue.create("key2", "value");
		assertNotEquals(value1, value2);
	}

	@Test
	public void equals_shouldHandleSameReference() throws Exception {
		ResultValue value = ResultValue.create("key", "value");
		assertEquals(value, value);
	}

	@Test
	public void equals_shouldHandleDifferentTypes() throws Exception {
		ResultValue value = ResultValue.create("key", "value");
		assertNotEquals(value, new Object());
	}

	@Test
	public void hashCode_shouldConsiderNameAndValue() throws Exception {
		ResultValue value1 = ResultValue.create("key", "value");
		ResultValue value2 = ResultValue.create("key", "value");
		assertEquals(value1.hashCode(), value2.hashCode());

		value1 = ResultValue.create("key", "value1");
		value2 = ResultValue.create("key", "value2");
		assertNotEquals(value1.hashCode(), value2.hashCode());

		value1 = ResultValue.create("key1", "value");
		value2 = ResultValue.create("key2", "value");
		assertNotEquals(value1.hashCode(), value2.hashCode());


		value1 = ResultValue.create("key", "value");
		value2 = ResultValue.create("key", null);
		assertNotEquals(value1.hashCode(), value2.hashCode());


		value1 = ResultValue.create(null, "value");
		value2 = ResultValue.create("key2", "value");
		assertNotEquals(value1.hashCode(), value2.hashCode());
	}

	@Test
	public void toString_shouldConsiderNameAndValue() throws Exception {
		String string = ResultValue.create("key1", "value1").toString();
		assertTrue(string.contains("key1"));
		assertTrue(string.contains("value1"));
	}

}
