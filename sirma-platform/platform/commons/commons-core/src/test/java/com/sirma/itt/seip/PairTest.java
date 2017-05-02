/**
 *
 */
package com.sirma.itt.seip;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.stream.Stream;

import org.testng.annotations.Test;

/**
 * @author BBonev
 *
 */
public class PairTest {

	@Test
	public void test_nonNullFirst() {
		assertFalse(Pair.nonNullFirst().test(null));
		assertFalse(Pair.nonNullFirst().test(new Pair<>()));
		assertTrue(Pair.nonNullFirst().test(new Pair<>("", "")));
	}

	@Test
	public void test_nonNullSecond() {
		assertFalse(Pair.nonNullSecond().test(null));
		assertFalse(Pair.nonNullSecond().test(new Pair<>()));
		assertTrue(Pair.nonNullSecond().test(new Pair<>("", "")));
	}

	@Test
	public void test_nonNull() {
		assertFalse(Pair.nonNull().test(null));
		assertFalse(Pair.nonNull().test(new Pair<>()));
		assertTrue(Pair.nonNull().test(new Pair<>("", "")));
	}

	@Test
	public void test_toMap() {
		Map<String, String> map = Stream.of(new Pair<>("key", "value")).collect(Pair.toMap());
		assertEquals(map.get("key"), "value");
	}
}
