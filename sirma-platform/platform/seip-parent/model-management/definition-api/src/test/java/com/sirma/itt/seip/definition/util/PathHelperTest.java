package com.sirma.itt.seip.definition.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for PathHelper.
 *
 * @author A. Kunchev
 */
public class PathHelperTest {

	@Test
	public void extractLastElementInPath_nullPath_null() {
		String result = PathHelper.extractLastElementInPath(null);
		assertNull(result);
	}

	@Test
	public void extractLastElementInPath_emptyPath_null() {
		String result = PathHelper.extractLastElementInPath("");
		assertNull(result);
	}

	@Test
	public void extractLastElementInPath_oneElementInPath_passedPath() {
		String result = PathHelper.extractLastElementInPath("path");
		assertEquals("path", result);
	}

	@Test
	public void extractLastElementInPath_multipleElementsInPath_lastElementOfPath() {
		String result = PathHelper.extractLastElementInPath("parent/subParent/element");
		assertEquals("element", result);
	}

}
