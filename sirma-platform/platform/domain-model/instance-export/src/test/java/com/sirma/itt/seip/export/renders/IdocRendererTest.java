package com.sirma.itt.seip.export.renders;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * IdocRenderer class tests
 * 
 * @author Hristo Lungov
 */
public class IdocRendererTest {

	/**
	 * Test extract valid value.
	 */
	@Test
	public static void testExtractValidValue() {
		Assert.assertEquals(IdocRenderer.extractValidValue(null), "");
		Assert.assertEquals(IdocRenderer.extractValidValue("Test"), "Test");
	}
}
