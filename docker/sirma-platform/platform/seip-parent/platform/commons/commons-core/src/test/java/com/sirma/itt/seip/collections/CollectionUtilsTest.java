package com.sirma.itt.seip.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.junit.Test;

/**
 * Tests for {@link CollectionUtils}.
 *
 * @author A. Kunchev
 */
public class CollectionUtilsTest {

	@Test
	public void isEmptyObjectArray_nullArray() {
		Object[] objects = null;
		assertTrue(CollectionUtils.isEmpty(objects));
	}

	@Test
	public void isEmptyObjectArray_emptyArray() {
		Object[] objects = {};
		assertTrue(CollectionUtils.isEmpty(objects));
	}

	@Test
	public void expectFalseForNonEmptyArray() {
		Object[] objects = new Object[] { new Object() };
		assertFalse(CollectionUtils.isEmpty(objects));
	}

	@Test
	public void doNotAddNullValueToBuilder() throws Exception {
		Builder<Object> builder = Stream.builder();
		CollectionUtils.addNonNullValue(builder, null);
		assertEquals(0L, builder.build().count());
	}

	@Test
	public void addNonNullValueToBuilder() throws Exception {
		Builder<Object> builder = Stream.builder();
		CollectionUtils.addNonNullValue(builder, new Object());
		assertEquals(1L, builder.build().count());
	}

	@Test
	public void testAddValueWithDuplicatesList() throws Exception {
		ArrayList<Object> data = new ArrayList<>();
		data.add("test");
		data.add("test");
		CollectionUtils.addValue(data, "value-single", false);
		assertEquals(3, data.size());
		CollectionUtils.addValue(data, Arrays.asList("value1", "value1"), false);
		assertEquals(5, data.size());
		CollectionUtils.addValue(data, "value-single", false);
		assertEquals(6, data.size());
	}

	@Test
	public void testAddValueWithDuplicatesSet() throws Exception {
		HashSet<Object> data = new HashSet<>();
		data.add("test");
		CollectionUtils.addValue(data, "value-single", false);
		assertEquals(2, data.size());
		CollectionUtils.addValue(data, Arrays.asList("value1", "value1"), false);
		assertEquals(3, data.size());
		CollectionUtils.addValue(data, "value-single", false);
		assertEquals(3, data.size());
	}

	@Test
	public void testAddValueWithoutDuplicatesSet() throws Exception {
		HashSet<Object> data = new HashSet<>();
		data.add("test");
		CollectionUtils.addValue(data, "value-single", true);
		assertEquals(2, data.size());
		CollectionUtils.addValue(data, Arrays.asList("value1", "value1"), true);
		assertEquals(3, data.size());
		CollectionUtils.addValue(data, "value-single", true);
		assertEquals(3, data.size());
	}

	@Test
	public void testAddValueWithoutDuplicatesList() throws Exception {
		ArrayList<Object> data = new ArrayList<>();
		data.add("test");
		CollectionUtils.addValue(data, "value-single", true);
		assertEquals(2, data.size());
		CollectionUtils.addValue(data, Arrays.asList("value1", "value1"), true);
		assertEquals(3, data.size());
		CollectionUtils.addValue(data, "value-single", true);
		assertEquals(3, data.size());
	}

	@Test
	public void testAddValueNull() throws Exception {
		ArrayList<Object> data = new ArrayList<>();
		data.add("test");
		CollectionUtils.addValue(data, null, true);
		assertEquals(1, data.size());
		CollectionUtils.addValue(data, "value-single", true);
		assertEquals(2, data.size());
		CollectionUtils.addValue(data, null, true);
		assertEquals(2, data.size());
	}
	
	@Test
	public void should_shuffleList() {
		List<String> list = Arrays.asList("a", "b", "c");

		// Using the same seed, the list will end up in the same state every time.
		CollectionUtils.shuffle(list, new Random(25565));
		assertEquals(list, Arrays.asList("b", "c", "a"));

		// Using the same seed, the list will just return to its initial state.
		CollectionUtils.deshuffle(list, new Random(25565));
		assertEquals(list, Arrays.asList("a", "b", "c"));
	}
}
