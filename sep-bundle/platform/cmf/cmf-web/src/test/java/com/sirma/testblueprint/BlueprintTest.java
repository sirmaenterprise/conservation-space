package com.sirma.testblueprint;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * Blueprint test class.
 * 
 * @author svelikov
 */
@Test
public class BlueprintTest {

	/**
	 * Class under test.
	 */
	protected final DummyAction dummyAction;

	/**
	 * Initializes the test.
	 */
	public BlueprintTest() {
		dummyAction = new DummyAction();
	}

	/**
	 * Test *** method.
	 */
	public void firstTest() {
		assertEquals(true, true);
		assertTrue(true);
		assertFalse(false);
		assertNull(null);
		assertNotNull(1);
	}
}
