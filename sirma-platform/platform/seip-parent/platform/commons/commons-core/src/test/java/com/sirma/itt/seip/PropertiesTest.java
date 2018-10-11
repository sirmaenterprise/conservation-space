/**
 *
 */
package com.sirma.itt.seip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for {@link Properties}.
 *
 * @author BBonev
 */
public class PropertiesTest {

	@Test
	public void test_add() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", "value2");

		assertEquals("value", properties.getProperties().get("key"));
	}

	@Test
	public void test_addAll() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.addAllProperties(Collections.singletonMap("key", "value"));
		properties.addAllProperties(null);

		assertEquals("value", properties.getProperties().get("key"));
	}

	@Test
	public void test_append_shouldAddValueIfNonIsSet() {
		Properties properties = new PropertiesDummy();
		assertTrue(properties.append("key", "value"));
		assertEquals("value", properties.get("key"));
	}

	@Test
	public void test_append_shouldAddValueIfNotEqual() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value1");
		assertTrue(properties.append("key", "value2"));
		assertEquals(Arrays.asList("value1", "value2"), properties.get("key"));
	}

	@Test
	public void test_append_shouldNotAddValueIfEqual() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		assertFalse(properties.append("key", "value"));
		assertEquals("value", properties.get("key"));
	}

	@Test
	public void test_append_shouldAddValueToCollection() {
		Properties properties = new PropertiesDummy();
		LinkedList<Serializable> values = new LinkedList<>(Arrays.asList("value1", "value2"));
		properties.add("key", values);
		assertTrue(properties.append("key", "value3"));
		assertEquals(Arrays.asList("value1", "value2", "value3"), properties.get("key"));
	}

	@Test
	public void test_append_shouldNotAddValueToCollectionIfAlreadyPresent() {
		Properties properties = new PropertiesDummy();
		LinkedList<Serializable> values = new LinkedList<>(Arrays.asList("value1", "value2"));
		properties.add("key", values);
		assertFalse(properties.append("key", "value2"));
		assertEquals(Arrays.asList("value1", "value2"), properties.get("key"));
	}

	@Test
	public void test_appendAll_shouldAddValuesIfNonIsSet() {
		Properties properties = new PropertiesDummy();
		assertTrue(properties.appendAll("key", Arrays.asList("value1", "value2")));
		assertEquals(Arrays.asList("value1", "value2"), properties.get("key"));
	}

	@Test
	public void test_appendAll_shouldAddValuesIfNotEqual() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value1");
		assertTrue(properties.appendAll("key", Arrays.asList("value2", "value3")));
		assertEquals(Arrays.asList("value1", "value2", "value3"), properties.get("key"));
	}

	@Test
	public void test_appendAll_shouldNotAddValuesIfEqual() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		assertFalse(properties.appendAll("key", Arrays.asList("value", "value")));
		assertEquals("value", properties.get("key"));
	}

	@Test
	public void test_appendAll_shouldAddValuesToCollection() {
		Properties properties = new PropertiesDummy();
		LinkedList<Serializable> values = new LinkedList<>(Arrays.asList("value1", "value2"));
		properties.add("key", values);
		assertTrue(properties.appendAll("key", Arrays.asList("value3", "value4")));
		assertEquals(Arrays.asList("value1", "value2", "value3", "value4"), properties.get("key"));
	}

	@Test
	public void test_appendAll_shouldNotAddValuesToCollectionIfAlreadyPresent() {
		Properties properties = new PropertiesDummy();
		LinkedList<Serializable> values = new LinkedList<>(Arrays.asList("value1", "value2"));
		properties.add("key", values);
		assertFalse(properties.appendAll("key", Arrays.asList("value2", "value1")));
		assertEquals(Arrays.asList("value1", "value2"), properties.get("key"));
	}

	@Test
	public void test_addIf_Predicate() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.addIf("key", "value", (key) -> true);
		properties.addIf("key2", "value2", (key) -> false);

		assertTrue(properties.getProperties().containsKey("key"));
		assertFalse(properties.getProperties().containsKey("key2"));
	}

	@Test
	public void test_addIf_BiPredicate() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.addIf("key", "value", (key, value) -> true);
		properties.addIf("key2", "value2", (key, value) -> false);

		assertTrue(properties.getProperties().containsKey("key"));
		assertFalse(properties.getProperties().containsKey("key2"));
	}

	@Test
	public void test_addIfNotNull() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.addIfNotNull("key", "value");
		properties.addIfNotNull("key2", null);

		assertTrue(properties.getProperties().containsKey("key"));
		assertFalse(properties.getProperties().containsKey("key2"));
	}

	@Test
	public void test_addIfNotNullOrEmpty() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.addIfNotNullOrEmpty("key", "value");
		properties.addIfNotNullOrEmpty("key2", null);
		properties.addIfNotNullOrEmpty("key3", "");

		assertTrue(properties.getProperties().containsKey("key"));
		assertFalse(properties.getProperties().containsKey("key2"));
		assertFalse(properties.getProperties().containsKey("key3"));
	}

	@Test
	public void test_addIfNotPresent() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", null);
		properties.add("key3", "");

		properties.addIfNotPresent("key", "value1");
		properties.addIfNotPresent("key2", "value1");
		properties.addIfNotPresent("key3", "value1");
		properties.addIfNotPresent("key4", "value1");

		assertEquals("value", properties.getProperties().get("key"));
		assertEquals(null, properties.getProperties().get("key2"));
		assertEquals("", properties.getProperties().get("key3"));
		assertEquals("value1", properties.getProperties().get("key4"));
	}

	@Test
	public void test_addIfNullMapping() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", null);
		properties.add("key3", "");

		properties.addIfNullMapping("key", "value1");
		properties.addIfNullMapping("key2", "value1");
		properties.addIfNullMapping("key3", "value1");
		properties.addIfNullMapping("key4", "value1");

		assertEquals("value", properties.getProperties().get("key"));
		assertEquals("value1", properties.getProperties().get("key2"));
		assertEquals("", properties.getProperties().get("key3"));
		assertEquals("value1", properties.getProperties().get("key4"));
	}

	@Test
	public void test_get() throws Exception {
		Properties properties = new PropertiesDummy();
		assertNull(properties.get("key"));
		properties.add("key", "value");

		assertEquals("value", properties.get("key"));
	}

	@Test
	public void test_getIfType() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");

		assertEquals("value", properties.get("key", String.class));
		assertNull(properties.get("key", Number.class));
	}

	@Test
	public void test_getWithDefaultValue() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");

		assertEquals("value", properties.get("key", "default"));
		assertEquals("default", properties.get("key2", "default"));
	}

	@Test
	public void test_getWithDefaultValueSupplier() throws Exception {
		Properties properties = new PropertiesDummy();

		assertEquals("default", properties.get("key", () -> "default"));

		properties.add("key", "value");

		assertEquals("value", properties.get("key", () -> "default"));
		assertEquals("default", properties.get("key2", () -> "default"));
	}

	@Test
	public void test_getIfTypeAndWithDefaultValueSupplier() throws Exception {
		Properties properties = new PropertiesDummy();

		assertEquals(Long.valueOf(1L), properties.get("key2", Long.class, () -> 1L));

		properties.add("key", "value");

		assertEquals("value", properties.get("key", String.class, () -> "default"));
		assertEquals(Long.valueOf(1L), properties.get("key", Long.class, () -> 1L));
		assertEquals(Long.valueOf(1L), properties.get("key2", Long.class, () -> 1L));
	}

	@Test
	public void test_getOrDefault() throws Exception {
		Properties properties = new PropertiesDummy();

		assertEquals(Long.valueOf(1L), properties.getOrDefault("key", Long.class, 1L));

		properties.add("key", "value");

		assertEquals("value", properties.getOrDefault("key", String.class, "default"));
		assertEquals(Long.valueOf(1L), properties.getOrDefault("key2", Long.class, 1L));
	}

	@Test
	public void test_getString() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", Long.valueOf(1L));

		assertEquals("value", properties.getString("key"));
		assertNull(properties.getString("key2"));
	}

	@Test
	public void test_getStringWithDefault() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", Long.valueOf(1L));

		assertEquals("value", properties.getString("key", "default"));
		assertEquals("default", properties.getString("key2", "default"));
	}

	@Test
	public void test_getStringWithDefaultSupplier() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.add("key2", Long.valueOf(1L));

		assertEquals("value", properties.getString("key", () -> "default"));
		assertEquals("default", properties.getString("key2", () -> "default"));
	}

	@Test
	public void test_getAs_function() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "2");

		assertEquals(Long.valueOf(2), properties.getAs("key", (v) -> Long.valueOf(v.toString())));
		assertNull(properties.getAs("key2", (v) -> Long.valueOf(v.toString())));
	}

	@Test
	public void test_getAs_functionAndDefault() throws Exception {
		Properties properties = new PropertiesDummy();

		assertEquals(Long.valueOf(1),
				properties.getAs("key", (v) -> Long.valueOf(v.toString()), () -> Long.valueOf(1L)));

		properties.add("key", "2");

		assertEquals(Long.valueOf(2), properties.getAs("key", (v) -> Long.valueOf(v.toString())));
		assertEquals(Long.valueOf(1),
				properties.getAs("key2", (v) -> Long.valueOf(v.toString()), () -> Long.valueOf(1L)));
	}

	@Test
	public void test_getAsCollection() throws Exception {
		Properties properties = new PropertiesDummy();
		Collection<Serializable> collection;

		// value not present
		collection = properties.getAsCollection("key", LinkedList::new);
		assertNotNull("Should not return null collection", collection);
		assertTrue(collection.isEmpty());

		properties.add("key", "value");

		collection = properties.getAsCollection("key", LinkedList::new);
		assertNotNull(collection);
		assertFalse(collection.isEmpty());
		assertEquals(1, collection.size());
		assertEquals("value", collection.iterator().next());

		properties.add("key", new ArrayList<>(Arrays.asList("value1", "value2")));

		collection = properties.getAsCollection("key", LinkedList::new);

		assertNotNull(collection);
		assertFalse(collection.isEmpty());
		assertEquals(2, collection.size());
		assertEquals("value1", collection.iterator().next());
	}

	@Test
	public void test_getAsString() throws Exception {
		Properties properties = new PropertiesDummy();

		assertNull(properties.getAsString("key"));

		properties.add("key", Long.valueOf(2));

		assertEquals("2", properties.getAsString("key"));
	}

	@Test
	public void test_getAsStringWithDefault() throws Exception {
		Properties properties = new PropertiesDummy();

		assertEquals("default", properties.getAsString("key", () -> "default"));

		properties.add("key", Long.valueOf(2));

		assertEquals("2", properties.getAsString("key", () -> "default"));
		assertEquals("default", properties.getAsString("key1", () -> "default"));
	}

	@Test
	public void test_getBoolean() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", Boolean.TRUE);

		assertTrue(properties.getBoolean("key"));
		assertFalse(properties.getBoolean("key2"));
	}

	@Test
	public void test_getBooleanDefaultValue() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", Boolean.TRUE);
		properties.add("key2", "test");

		assertTrue(properties.getBoolean("key", false));
		assertTrue(properties.getBoolean("key2", true));
		assertTrue(properties.getBoolean("key3", true));
	}

	@Test
	public void test_getBooleanWithDefaultValueSupplier() throws Exception {
		Properties properties = new PropertiesDummy();
		assertTrue(properties.getBoolean("key2", () -> true));

		properties.add("key", Boolean.TRUE);
		properties.add("key2", "test");

		assertTrue(properties.getBoolean("key", () -> false));
		assertTrue(properties.getBoolean("key3", () -> true));
	}

	@Test
	public void test_getBooleanWithLiteral() {
		Properties properties = new PropertiesDummy();
		properties.add("key1", "true");
		properties.add("key2", "false");

		assertTrue(properties.getBoolean("key1", () -> false));
		assertFalse(properties.getBoolean("key2", () -> true));
	}

	@Test
	public void test_remove() throws Exception {
		Properties properties = new PropertiesDummy();
		assertNull(properties.remove("key1"));

		properties.add("key1", "value1");
		properties.add("key2", "value2");
		properties.add("key3", "value3");

		assertEquals("value2", properties.remove("key2"));
		assertNull(properties.remove("key4"));
	}

	@Test
	public void test_removeValue_shouldRemoveValueOnlyIfMatches() throws Exception {
		Properties properties = new PropertiesDummy();
		assertFalse(properties.remove("key1", "value"));

		properties.add("key1", "value1");
		properties.add("key2", "value2");

		assertTrue(properties.remove("key2", "value2"));
	}

	@Test
	public void test_removeValue_shouldReplaceCollectionWithTheLastRemainingValue() throws Exception {
		Properties properties = new PropertiesDummy();

		properties.add("key3", new LinkedList<>(Arrays.asList("value3", "value4")));

		assertTrue(properties.remove("key3", "value4"));
		assertEquals("value3", properties.get("key3"));
	}

	@Test
	public void test_removeValue_shouldRemoveEmptyCollectionValue() throws Exception {
		Properties properties = new PropertiesDummy();

		properties.add("key4", new LinkedList<>(Collections.singletonList("value4")));

		assertTrue(properties.remove("key4", "value4"));
		assertNull(properties.get("key4"));
	}

	@Test
	public void test_removeMultiple() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.removeProperties(Collections.emptyList());
		properties.removeProperties(null);
		properties.removeProperties(Collections.singleton("key1"));

		properties.add("key1", "value1");
		properties.add("key2", "value2");
		properties.add("key3", "value3");

		properties.removeProperties(Arrays.asList("key2", "key3", "key4"));

		assertNull(properties.get("key2"));
		assertNull(properties.get("key3"));
	}

	@Test
	public void test_isPropertyPresent() throws Exception {
		Properties properties = new PropertiesDummy();
		assertFalse(properties.isPropertyPresent("key"));
		properties.add("key1", "value");
		properties.add("key2", null);
		assertTrue(properties.isPropertyPresent("key1"));
		assertTrue(properties.isPropertyPresent("key2"));
	}

	@Test
	public void test_isValueNull() throws Exception {
		Properties properties = new PropertiesDummy();
		assertTrue(properties.isValueNull("key"));
		properties.add("key1", "value");
		properties.add("key2", null);
		assertFalse(properties.isValueNull("key1"));
		assertTrue(properties.isValueNull("key2"));
	}

	@Test
	public void test_isValueNotNull() throws Exception {
		Properties properties = new PropertiesDummy();
		assertFalse(properties.isValueNotNull("key"));
		properties.add("key1", "value");
		properties.add("key2", null);
		assertTrue(properties.isValueNotNull("key1"));
		assertFalse(properties.isValueNotNull("key2"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void test_preventModifications_empty() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.preventModifications();
		properties.add("key", "value");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void test_preventModifications_nonEmpty() throws Exception {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value");
		properties.preventModifications();
		assertTrue(properties.isPropertyPresent("key"));
		properties.add("key1", "value1");
	}

	@Test
	public void test_getAsDouble() throws Exception {
		Properties properties = new PropertiesDummy();
		assertEquals(Double.valueOf(0.0), properties.getAsDouble("missingValue"));
		properties.add("doubleValue", Double.valueOf(5.32));
		properties.add("doubleAsString", "332.34");
		assertEquals(Double.valueOf(5.32), properties.getAsDouble("doubleValue"));
		assertEquals(Double.valueOf(332.34), properties.getAsDouble("doubleAsString"));
	}

	@Test
	public void test_getDouble() throws Exception {
		Properties properties = new PropertiesDummy();
		assertEquals(Double.valueOf(0.0), Double.valueOf(properties.getDouble("missingValue")));
		properties.add("doubleValue", Double.valueOf(5.32));
		properties.add("doubleAsString", "332.34");
		assertEquals(Double.valueOf(5.32), Double.valueOf(properties.getDouble("doubleValue")));
		assertEquals(Double.valueOf(0.0), Double.valueOf(properties.getDouble("doubleAsString")));
	}

	@Test
	public void containsValue_ShouldHandleNullValues() {
		Properties properties = new PropertiesDummy();
		assertFalse(properties.containsValue("key", "value"));
		properties.add("key1", "value1");
		assertFalse(properties.containsValue("key1", null));
		assertTrue(properties.containsValue("key", null));
	}

	@Test
	public void containsValue_ShouldHandleCollectionValue() {
		Properties properties = new PropertiesDummy();
		properties.add("key", (Serializable) Arrays.asList("value1", "value2"));
		assertTrue(properties.containsValue("key", "value1"));
		assertTrue(properties.containsValue("key", "value2"));
		assertFalse(properties.containsValue("key", "value3"));
		assertFalse(properties.containsValue("key1", "value1"));
		assertTrue(properties.containsValue("key", (Serializable) Collections.singletonList("value2")));
		assertTrue(properties.containsValue("key", (Serializable) Collections.singletonList("value1")));
		assertTrue(properties.containsValue("key", (Serializable) Arrays.asList("value1", "value2")));
	}

	@Test
	public void containsValue_ShouldHandleCollectionArgument() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value1");
		assertTrue(properties.containsValue("key", "value1"));
		assertTrue(properties.containsValue("key", (Serializable) Collections.singletonList("value1")));
		assertFalse(properties.containsValue("key", (Serializable) Collections.singletonList("value2")));
		assertFalse(properties.containsValue("key", (Serializable) Arrays.asList("value1", "value2")));
	}

	@Test
	public void matchesValue_ShouldHandleNullValues() {
		Properties properties = new PropertiesDummy();
		assertFalse(properties.matchesValue("key", "value"));
		properties.add("key1", "value1");
		assertFalse(properties.matchesValue("key1", null));
		assertTrue(properties.matchesValue("key", null));
	}

	@Test
	public void matchesValue_ShouldHandleCollectionValue() {
		Properties properties = new PropertiesDummy();
		properties.add("key", (Serializable) Arrays.asList("value1", "value2"));
		properties.add("key1", (Serializable) Collections.singletonList("value1"));
		properties.add("key2", "value3");
		assertFalse(properties.matchesValue("key", "value1"));
		assertTrue(properties.matchesValue("key2", "value3"));
		assertFalse(properties.matchesValue("key", "value2"));
		assertFalse(properties.matchesValue("key", "value3"));
		assertTrue(properties.matchesValue("key1", "value1"));
		assertTrue(properties.matchesValue("key2", (Serializable) Collections.singletonList("value3")));
		assertFalse(properties.matchesValue("key", (Serializable) Collections.singletonList("value1")));
		assertTrue(properties.matchesValue("key", (Serializable) Arrays.asList("value1", "value2")));
	}

	@Test
	public void matchesValue_ShouldHandleCollectionArgument() {
		Properties properties = new PropertiesDummy();
		properties.add("key", "value1");
		assertTrue(properties.matchesValue("key", "value1"));
		assertTrue(properties.matchesValue("key", (Serializable) Collections.singletonList("value1")));
		assertFalse(properties.matchesValue("key", (Serializable) Collections.singletonList("value2")));
		assertFalse(properties.matchesValue("key", (Serializable) Arrays.asList("value1", "value2")));
	}

	/**
	 * Dummy implementation of {@link Properties} to test the default methods.
	 *
	 * @author BBonev
	 */
	private static class PropertiesDummy implements Properties {

		private Map<String, Serializable> properties;

		@Override
		public Map<String, Serializable> getProperties() {
			return properties;
		}

		@Override
		public void setProperties(Map<String, Serializable> properties) {
			this.properties = properties;
		}
	}
}
