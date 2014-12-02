package com.sirma.itt.emf.web.label;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link LabelsAsJSResourceHandler}
 * 
 * @author Adrian Mitev
 */
public class LabelsAsJSResourceHandlerTest {

	private LabelsAsJSResourceHandler resourceHandler;

	/**
	 * Initializes CUT.
	 */
	@BeforeClass
	public void init() {
		resourceHandler = new LabelsAsJSResourceHandler();
	}

	/**
	 * Tests getLanguageFromPath() method.
	 */
	@Test
	public void testGetLanguageFromPath() {
		String result = resourceHandler.getLanguageFromPath("labels_bg.js");
		Assert.assertEquals("bg", result);
	}

}
