package com.sirma.itt.seip.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

/**
 * Test for the default methods of {@link ResultItem}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/07/2017
 */
public class ResultItemTest {

	@Test
	public void getAs_shouldReturnDefaultValueIfNull() throws Exception {
		Serializable value = new SimpleResultItem(null).getAs("", Serializable.class, "default");
		assertEquals("default", value);
	}

	@Test
	public void getAs_shouldValueIfNotNull() throws Exception {
		Serializable value = new SimpleResultItem("value").getAs("", Serializable.class, "default");
		assertEquals("value", value);
	}

	@Test
	public void getString_shouldReturntringValue() throws Exception {
		String value = new SimpleResultItem("value").getString("");
		assertEquals("value", value);
	}

	@Test
	public void getBooleanOrFalse() throws Exception {
		boolean value = new SimpleResultItem(Boolean.TRUE).getBooleanOrFalse("");
		assertTrue(value);
	}

	@Test
	public void getBooleanOrTrue() throws Exception {
		boolean value = new SimpleResultItem(Boolean.FALSE).getBooleanOrFalse("");
		assertFalse(value);
	}

	@Test
	public void getBoolean() throws Exception {
		boolean value = new SimpleResultItem(Boolean.TRUE).getBoolean("", Boolean.FALSE);
		assertTrue(value);
	}

	@Test
	public void getInt() throws Exception {
		int value = new SimpleResultItem(1).getInt("");
		assertEquals(1, value);
	}

	@Test
	public void getIntWithDefault() throws Exception {
		int value = new SimpleResultItem(1).getInt("", 2);
		assertEquals(1, value);
		value = new SimpleResultItem(null).getInt("", 2);
		assertEquals(2, value);
	}

	@Test
	public void getLong() throws Exception {
		long value = new SimpleResultItem(1L).getLong("");
		assertEquals(1L, value);
	}

	@Test
	public void getLongWithDefault() throws Exception {
		long value = new SimpleResultItem(1L).getLong("", 2L);
		assertEquals(1L, value);
		value = new SimpleResultItem(null).getLong("", 2L);
		assertEquals(2L, value);
	}

	private static class SimpleResultItem implements ResultItem {

		private final Serializable value;

		private SimpleResultItem(Serializable value) {
			this.value = value;
		}

		@Override
		public Iterator<ResultValue> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Set<String> getValueNames() {
			return Collections.emptySet();
		}

		@Override
		public boolean hasValue(String name) {
			return false;
		}

		@Override
		public ResultValue getValue(String name) {
			return null;
		}

		@Override
		public Serializable getResultValue(String name) {
			return value;
		}

		@Override
		public int size() {
			return 0;
		}
	}
}
