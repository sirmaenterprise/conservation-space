package com.sirma.itt.emf.web.treeHeader;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The Class PathBuilderTest.
 * 
 * @author svelikov
 */
@Test
public class PathBuilderTest {

	private final PathBuilder pathBuilder;

	/**
	 * Instantiates a new path builder test.
	 */
	public PathBuilderTest() {
		pathBuilder = new PathBuilder();
	}

	/**
	 * Test if path builder assembles correct output.
	 */
	public void buildPathTest() {
		String path = pathBuilder.buildPath(null);
		Assert.assertEquals(path, "[]");
	}

}
