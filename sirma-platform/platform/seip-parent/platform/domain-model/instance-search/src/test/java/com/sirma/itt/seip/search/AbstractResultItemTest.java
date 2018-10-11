package com.sirma.itt.seip.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import org.junit.Test;

/**
 * Test for the equals and hashcode methods of {@link AbstractResultItem}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/07/2017
 */
public class AbstractResultItemTest {

	@Test
	public void equals_shouldCheckAllKeysAndValues() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		Map<String, Serializable> values2 = new HashMap<>();
		values2.put("key1", "value1");
		values2.put("key2", 2);
		SimpleResultItem item2 = new SimpleResultItem(values2);

		assertTrue(item1.equals(item2));
	}

	@Test
	public void equals_shouldReturnFalseOnNotEqualValues() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		Map<String, Serializable> values2 = new HashMap<>();
		values2.put("key1", "value1");
		values2.put("key2", 3);
		SimpleResultItem item2 = new SimpleResultItem(values2);

		assertFalse(item1.equals(item2));
	}

	@Test
	public void equals_shouldIgnoreSize() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		Map<String, Serializable> values2 = new HashMap<>();
		values2.put("key1", "value1");
		values2.put("key2", 2);
		SimpleResultItem item2 = new SimpleResultItem(values2);

		assertTrue(item1.equals(item2));
	}

	@Test
	public void equals_shouldReturnFalseIfNotResultItem() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		assertFalse(item1.equals(new Object()));
	}

	@Test
	public void equals_shouldReturnTrueIfSameObject() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		assertTrue(item1.equals(item1));
	}

	@Test
	public void equals_shouldReturnFalseIfNull() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		assertFalse(item1.equals(null));
	}

	@Test
	public void hashCode_shouldCheckAllKeysAndValues() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		Map<String, Serializable> values2 = new HashMap<>();
		values2.put("key1", "value1");
		values2.put("key2", 2);
		SimpleResultItem item2 = new SimpleResultItem(values2);

		assertEquals("Hash codes should be equal", item1.hashCode(), item2.hashCode());

		values2.put("key2", 3);
		assertNotEquals("Hash codes should be different", item1.hashCode(), item2.hashCode());
	}

	@Test
	public void hashCode_shouldIgnoreNullValues() throws Exception {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		Map<String, Serializable> values2 = new HashMap<>();
		values2.put("key1", "value1");
		values2.put("key2", 2);
		SimpleResultItem item2 = new SimpleResultItem(values2);

		assertEquals("Hash codes should be equal", item1.hashCode(), item2.hashCode());
	}

	@Test
	public void toString_shouldProvideAllEntries() {
		Map<String, Serializable> values1 = new HashMap<>();
		values1.put("key1", "value1");
		values1.put("key2", 2);
		values1.put("key3", null);
		SimpleResultItem item1 = new SimpleResultItem(values1);

		String string = item1.toString();
		assertTrue(string.contains("value1"));
		assertTrue(string.contains("=2"));
	}

	private static class SimpleResultItem extends AbstractResultItem {

		private Map<String, Serializable> values;

		public SimpleResultItem(Map<String, Serializable> values) {
			this.values = values;
		}

		@Override
		public Iterator<ResultValue> iterator() {
			return new SimpleResultItemIterator(values.entrySet().stream().filter(e -> e.getValue() != null).iterator());
		}

		@Override
		public Set<String> getValueNames() {
			return values.keySet();
		}

		@Override
		public boolean hasValue(String name) {
			return values.containsKey(name);
		}

		@Override
		public ResultValue getValue(String name) {
			return ResultValue.create(name, getResultValue(name));
		}

		@Override
		public Serializable getResultValue(String name) {
			return values.get(name);
		}

		@Override
		public int size() {
			return values.size();
		}

		private class SimpleResultItemIterator implements Iterator<ResultValue> {

			private final Iterator<Map.Entry<String, Serializable>> delegate;

			SimpleResultItemIterator(Iterator<Map.Entry<String, Serializable>> iterator) {
				this.delegate = iterator;
			}

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public ResultValue next() {
				Map.Entry<String, Serializable> entry = delegate.next();
				return ResultValue.create(entry.getKey(), entry.getValue());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}
	}

}
