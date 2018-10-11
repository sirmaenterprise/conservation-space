package com.sirma.itt.seip.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

/**
 * Test for {@link SimpleResultItem}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/07/2017
 */
public class SimpleResultItemTest {
	@Test
	public void iterator_shouldReturnNonNullValues() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		int count = 0;
		for (ResultValue value : item) {
			count++;
			assertNotNull(value.getValue());
		}
		assertEquals(2, count);
	}

	@Test
	public void getValueNames_shouldReturnAllValueNames() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		Set<String> valueNames = item.getValueNames();
		assertEquals(3, valueNames.size());
		assertTrue(valueNames.containsAll(Arrays.asList("key1", "key2", "key3")));
	}

	@Test
	public void hasValue_shouldReportNullValuesAsWell() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		assertTrue(item.hasValue("key1"));
		assertTrue(item.hasValue("key2"));
		assertTrue(item.hasValue("key3"));
	}

	@Test
	public void getValue_shouldReturnNullForNullValue() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		assertNotNull(item.getValue("key1"));
		assertNull(item.getValue("key2"));
		assertNotNull(item.getValue("key3"));
	}

	@Test
	public void getResultValue() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		assertNotNull(item.getResultValue("key1"));
		assertNull(item.getResultValue("key2"));
		assertNotNull(item.getResultValue("key3"));
	}

	@Test
	public void size_shouldCountNonNullValues() throws Exception {
		SimpleResultItem item = buildSimpleItem();
		assertEquals(2, item.size());
	}

	private static SimpleResultItem buildSimpleItem() {
		return SimpleResultItem.create().add("key1", "value1").add("key2", null).add("key3", 2);
	}
}
