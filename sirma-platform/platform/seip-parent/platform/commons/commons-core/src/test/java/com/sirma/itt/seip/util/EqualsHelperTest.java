/**
 *
 */
package com.sirma.itt.seip.util;

import static com.sirma.itt.seip.util.EqualsHelper.diffCollections;
import static com.sirma.itt.seip.util.EqualsHelper.hasNotNullProperty;
import static com.sirma.itt.seip.util.EqualsHelper.hasNullProperty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;

/**
 * @author BBonev
 */
@Test
public class EqualsHelperTest {

	public void testnullSafeEquals() {
		assertTrue(EqualsHelper.nullSafeEquals(null, null));
		assertTrue(EqualsHelper.nullSafeEquals("test", "test"));

		assertTrue(EqualsHelper.nullSafeEquals(Integer.valueOf(5), Integer.valueOf(5)));

		assertFalse(EqualsHelper.nullSafeEquals(null, "test"));
		assertFalse(EqualsHelper.nullSafeEquals("test", null));
		assertFalse(EqualsHelper.nullSafeEquals("test", "test3"));
		assertFalse(EqualsHelper.nullSafeEquals("test", Integer.valueOf(2)));
	}

	public void testnullSafeEquals_string() {
		assertTrue(EqualsHelper.nullSafeEquals(null, null, true));
		assertTrue(EqualsHelper.nullSafeEquals("test", "test", false));
		assertTrue(EqualsHelper.nullSafeEquals("test", "TEST", true));

		assertFalse(EqualsHelper.nullSafeEquals(null, "test", true));
		assertFalse(EqualsHelper.nullSafeEquals("test", null, true));
		assertFalse(EqualsHelper.nullSafeEquals("test", "test3", true));

		assertFalse(EqualsHelper.nullSafeEquals("test", "TeSt", false));
		assertTrue(EqualsHelper.nullSafeEquals("test", "TeSt", true));
	}

	public void test_binaryStreamEquals() throws IOException {
		ByteArrayInputStream stream1 = new ByteArrayInputStream("".getBytes("utf-8"));
		ByteArrayInputStream stream2 = new ByteArrayInputStream("".getBytes("utf-8"));
		assertTrue(EqualsHelper.binaryStreamEquals(stream1, stream2));

		stream1 = new ByteArrayInputStream("testst".getBytes("utf-8"));
		stream2 = new ByteArrayInputStream("".getBytes("utf-8"));
		assertFalse(EqualsHelper.binaryStreamEquals(stream1, stream2));

		stream1 = new ByteArrayInputStream("".getBytes("utf-8"));
		stream2 = new ByteArrayInputStream("testst".getBytes("utf-8"));
		assertFalse(EqualsHelper.binaryStreamEquals(stream1, stream2));

		stream1 = new ByteArrayInputStream("qeqweqweqe".getBytes("utf-8"));
		stream2 = new ByteArrayInputStream("tesqewqweqqweqwtst".getBytes("utf-8"));
		assertFalse(EqualsHelper.binaryStreamEquals(stream1, stream2));

		stream1 = new ByteArrayInputStream("qeqweqweqedsdsdewe".getBytes("utf-8"));
		stream2 = new ByteArrayInputStream("tesqewqweqqweqwtst".getBytes("utf-8"));
		assertFalse(EqualsHelper.binaryStreamEquals(stream1, stream2));

		stream1 = new ByteArrayInputStream("qeqweqweqedsdsdewe".getBytes("utf-8"));
		stream2 = new ByteArrayInputStream("qeqweqweqedsdsdewe".getBytes("utf-8"));
		assertTrue(EqualsHelper.binaryStreamEquals(stream1, stream2));
	}

	public void test_getMapComparison() {
		Map<String, Serializable> map1 = new HashMap<>();
		Map<String, Serializable> map2 = new HashMap<>();

		Map<String, MapValueComparison> diff = EqualsHelper.getMapComparison(map1, map2);
		assertTrue(diff.isEmpty());

		map1.put("key1", "test");
		diff = EqualsHelper.getMapComparison(map1, map2);
		assertFalse(diff.isEmpty());
		assertEquals(diff.get("key1"), MapValueComparison.LEFT_ONLY);

		map2.put("key1", "test");
		diff = EqualsHelper.getMapComparison(map1, map2);
		assertFalse(diff.isEmpty());
		assertEquals(diff.get("key1"), MapValueComparison.EQUAL);

		map1.remove("key1");
		diff = EqualsHelper.getMapComparison(map1, map2);
		assertFalse(diff.isEmpty());
		assertEquals(diff.get("key1"), MapValueComparison.RIGHT_ONLY);

		map1.put("key1", "test2");
		diff = EqualsHelper.getMapComparison(map1, map2);
		assertFalse(diff.isEmpty());
		assertEquals(diff.get("key1"), MapValueComparison.NOT_EQUAL);
	}

	public void test_diffValues() {
		assertEquals(EqualsHelper.diffValues("", null), MapValueComparison.LEFT_ONLY);
		assertEquals(EqualsHelper.diffValues(null, ""), MapValueComparison.RIGHT_ONLY);
		assertEquals(EqualsHelper.diffValues(null, null), null);
		assertEquals(EqualsHelper.diffValues("test", "test"), MapValueComparison.EQUAL);
		assertEquals(EqualsHelper.diffValues("tsts", "test"), MapValueComparison.NOT_EQUAL);
	}

	public void test_nullCompare() {
		assertEquals(EqualsHelper.nullCompare(null, null), 0);
		assertEquals(EqualsHelper.nullCompare(null, ""), 1);
		assertEquals(EqualsHelper.nullCompare("", null), -1);
		assertEquals(EqualsHelper.nullCompare("", ""), 2);
	}

	public void test_nullSafeCompare() {
		assertEquals(EqualsHelper.nullSafeCompare(null, null), 0);
		assertEquals(EqualsHelper.nullSafeCompare(null, ""), 1);
		assertEquals(EqualsHelper.nullSafeCompare("", null), -1);
		assertEquals(EqualsHelper.nullSafeCompare("", ""), 0);
		assertEquals(EqualsHelper.nullSafeCompare("test", "test"), 0);

		assertEquals(EqualsHelper.nullSafeCompare("test", null), -1);
		assertEquals(EqualsHelper.nullSafeCompare(null, "test"), 1);
	}

	public void test_diffLists() {
		EqualsHelper.diffLists(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3));

		EqualsHelper.diffLists(Arrays.asList(1, 3, 4), Arrays.asList(1, 2, 3));

		EqualsHelper.diffLists(Arrays.asList(2, 2, 3), Arrays.asList(4, 2, 3));

		EqualsHelper.diffLists(Arrays.asList(2), Arrays.asList(4, 2, 3));

		EqualsHelper.diffLists(Arrays.asList(2, 2, 3), Arrays.asList(3));

		EqualsHelper.diffLists(Arrays.asList(5, 2, 3), Arrays.asList(3, 4, 1));

		EqualsHelper.diffLists(Arrays.asList(5, 21, 332), Arrays.asList(3, 224, 321));

		EqualsHelper.diffLists(Arrays.asList(5, 21, 332, 333, 444), Arrays.asList(3, 32, 321, 222, 444));
	}

	public void test_getMapDifferenceReport() {
		Map<String, Serializable> map1 = new HashMap<>();
		Map<String, Serializable> map2 = new HashMap<>();

		String diff = EqualsHelper.getMapDifferenceReport(map1, map2);
		assertNull(diff);

		map1.put("key1", "test");
		diff = EqualsHelper.getMapDifferenceReport(map1, map2);
		assertNotNull(diff);

		map2.put("key1", "test");
		diff = EqualsHelper.getMapDifferenceReport(map1, map2);
		assertNull(diff);

		map1.remove("key1");
		diff = EqualsHelper.getMapDifferenceReport(map1, map2);
		assertNotNull(diff);

		map1.put("key1", "test2");
		diff = EqualsHelper.getMapDifferenceReport(map1, map2);
		assertNotNull(diff);
	}

	/**
	 * Test null compare.
	 */
	public void testNullCompare() {

		List<Integer> list = Arrays.asList(5, 3, 1, 2, 7, null, 3);
		Collections.sort(list, (arg0, arg1) -> {
			int compare = EqualsHelper.nullCompare(arg0, arg1);
			if (compare != 2) {
				return compare;
			}
			return arg0.compareTo(arg1);
		});

		Assert.assertNotNull(list.get(0));
		Assert.assertNull(list.get(list.size() - 1));

		Assert.assertEquals(EqualsHelper.nullCompare(null, null), 0);
		Assert.assertEquals(EqualsHelper.nullCompare("", null), -1);
		Assert.assertEquals(EqualsHelper.nullCompare(null, ""), 1);
		Assert.assertEquals(EqualsHelper.nullCompare("", ""), 2);
	}

	/**
	 * Test null safe compare.
	 */
	public void testNullSafeCompare() {

		List<Integer> list = Arrays.asList(5, 3, 1, 2, 7, null, 3);
		Collections.sort(list, (arg0, arg1) -> {
			int compare = EqualsHelper.nullSafeCompare(arg0, arg1);
			return compare;
		});

		Assert.assertNotNull(list.get(0));
		Assert.assertEquals(list.get(0).intValue(), 1);
		Assert.assertNull(list.get(list.size() - 1));

		Assert.assertEquals(EqualsHelper.nullSafeCompare(null, null), 0);
		Assert.assertEquals(EqualsHelper.nullSafeCompare("2", null), -1);
		Assert.assertEquals(EqualsHelper.nullSafeCompare(null, "2"), 1);
		Assert.assertEquals(EqualsHelper.nullSafeCompare("22", "22"), 0);
	}

	@Test
	public void test_equalsToObject() {
		assertTrue(EqualsHelper.equalsTo(null).test(null));
		assertTrue(EqualsHelper.equalsTo(2).test(2));
		assertFalse(EqualsHelper.equalsTo(2).test(3));
		assertTrue(EqualsHelper.equalsTo("test").test("test"));
		assertFalse(EqualsHelper.equalsTo("test").test("Test"));
		assertFalse(EqualsHelper.equalsTo("test").test(null));
		assertFalse(EqualsHelper.equalsTo(null).test("test"));
	}

	@Test
	public void test_equalsToString() {
		assertTrue(EqualsHelper.equalsToIgnoreCase(null).test(null));
		assertTrue(EqualsHelper.equalsToIgnoreCase("test").test("test"));
		assertTrue(EqualsHelper.equalsToIgnoreCase("test").test("Test"));
		assertFalse(EqualsHelper.equalsToIgnoreCase("test").test(null));
		assertFalse(EqualsHelper.equalsToIgnoreCase(null).test("test"));
	}

	@Test
	public void test_diffCollections() throws Exception {
		Collection<Integer> right = Arrays.asList(1, 2, 3, 4, 5);
		Collection<Integer> left = Arrays.asList(1, 6, 7);
		Pair<Set<Integer>, Set<Integer>> diff = diffCollections(left, right);
		assertNotNull(diff);
		assertNotNull(diff.getFirst());
		assertNotNull(diff.getSecond());

		assertEquals(diff.getFirst(), new HashSet<>(Arrays.asList(6, 7)));
		assertEquals(diff.getSecond(), new HashSet<>(Arrays.asList(2, 3, 4, 5)));
	}

	@Test
	public void test_hasNotNullProperty() throws Exception {
		assertTrue(hasNotNullProperty(object -> "nonNullValue").test("someObject"));
		assertFalse(hasNotNullProperty(object -> null).test("someObject"));
	}

	@Test
	public void test_hasNullProperty() throws Exception {
		assertFalse(hasNullProperty(object -> "nonNullValue").test("someObject"));
		assertTrue(hasNullProperty(object -> null).test("someObject"));
	}
}
