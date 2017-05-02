package com.sirma.itt.seip.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link Range}
 *
 * @author BBonev
 */
public class RangeTest {

	@Test
	public void parse() throws Exception {
		Range range = Range.fromString("bytes=0-999");
		assertNotNull(range);
		assertEquals(0L, range.getFrom());
		assertEquals(999L, range.getTo());
		assertEquals(1000L, range.getRangeLength());
		assertEquals(Range.BYTES, range.getUnit());
		assertFalse(range.isToTheEnd());
		assertTrue(range.isInBytes());
	}

	@Test
	public void parse_noUnit() throws Exception {
		Range range = Range.fromString("0-999");
		assertNotNull(range);
		assertEquals(0L, range.getFrom());
		assertEquals(999L, range.getTo());
		assertEquals(1000L, range.getRangeLength());
		assertEquals(Range.BYTES, range.getUnit());
		assertFalse(range.isToTheEnd());
		assertTrue(range.isInBytes());
	}

	@Test
	public void parse_open() throws Exception {
		Range range = Range.fromString("bytes=1000-");
		assertNotNull(range);
		assertEquals(1000L, range.getFrom());
		assertEquals(-1L, range.getTo());
		assertEquals(-1L, range.getRangeLength());
		assertEquals(Range.BYTES, range.getUnit());
		assertTrue(range.isToTheEnd());
		assertTrue(range.isInBytes());
	}

	@Test
	public void parse_open_noSeparator() throws Exception {
		Range range = Range.fromString("bytes=1000");
		assertNotNull(range);
		assertEquals(1000L, range.getFrom());
		assertEquals(-1L, range.getTo());
		assertEquals(-1L, range.getRangeLength());
		assertEquals(Range.BYTES, range.getUnit());
		assertTrue(range.isToTheEnd());
		assertTrue(range.isInBytes());
	}

	@Test
	public void parse_Invalid() throws Exception {
		assertEquals(Range.ALL, Range.fromString(""));
		assertEquals(Range.ALL, Range.fromString("   "));
		assertEquals(Range.ALL, Range.fromString(null));
	}

	@Test
	public void testEquals() throws Exception {
		assertTrue(new Range(null, 10, -1).equals(new Range(Range.BYTES, 10, -1)));
		assertTrue(new Range(null, 10, 11).equals(new Range(Range.BYTES, 10, 11)));
		assertFalse(new Range(null, 10, 11).equals(new Object()));

		assertFalse(new Range(null, 10, 11).equals(new Range("bits", 10, 11)));
		assertFalse(new Range(null, 10, 11).equals(new Range(Range.BYTES, 10, 12)));
		assertFalse(new Range(null, 10, 11).equals(new Range(Range.BYTES, 9, 11)));
	}

	@Test
	public void testHashCode() throws Exception {
		assertEquals(new Range(null, 10, -1).hashCode(), new Range(Range.BYTES, 10, -1).hashCode());
		assertNotEquals(new Range(null, 10, 11).hashCode(), new Range(Range.BYTES, 10, -1).hashCode());
		assertNotEquals(new Range(null, 10, 11).hashCode(), new Range("bits", 10, 11).hashCode());
		assertNotEquals(new Range(null, 10, -1).hashCode(), new Range(Range.BYTES, 10, 11).hashCode());
		assertNotEquals(new Range(null, 10, -1).hashCode(), new Range(Range.BYTES, 9, -1).hashCode());
	}

	@Test
	public void asResponseString() throws Exception {
		assertEquals("bytes 0-999/1500", new Range(null, 0, 999).asResponse(1500));
		assertEquals("bytes 1000-1499/1500", new Range(null, 1000, -1).asResponse(1500));
	}

	@Test
	public void testToString() throws Exception {
		assertNotNull(new Range(null, 0, -1).toString());
		assertNotNull(new Range(Range.BYTES, 0, 10).toString());
	}

	@Test
	public void testIsAllRequested() throws Exception {
		assertTrue(new Range(null, 0, -1L).isAllRequested());
		assertFalse(new Range(null, 1, -1L).isAllRequested());
		assertFalse(new Range(null, 0, 10).isAllRequested());
	}
}
