package com.sirma.sep.export.renders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * IdocRenderer class tests
 *
 * @author Hristo Lungov
 */
@SuppressWarnings("static-method")
public class IdocRendererTest {

	/**
	 * Test extract valid value.
	 */
	@Test
	public void testExtractValidValue() {
		assertEquals("", IdocRenderer.extractValidValue(null));
		assertEquals("Test", IdocRenderer.extractValidValue("Test"));
	}
}
