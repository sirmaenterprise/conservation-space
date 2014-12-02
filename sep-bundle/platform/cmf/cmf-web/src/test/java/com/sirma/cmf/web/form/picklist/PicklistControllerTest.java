package com.sirma.cmf.web.form.picklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * Test for PicklistController.
 * 
 * @author svelikov
 */
public class PicklistControllerTest {

	/** The controller. */
	private final PicklistController controller;

	/**
	 * Instantiates a new picklist controller test.
	 */
	public PicklistControllerTest() {
		controller = new PicklistController();
	}

	/**
	 * Test for loadItems method.
	 */
	public void loadItemsTest() {
		// TODO: implement
	}

	/**
	 * Test for loadItemsList method.
	 */
	public void loadItemsListTest() {
		// TODO: implement
	}

	/**
	 * Test for get as string method.
	 */
	public void getAsStringTest() {
		// String actual = controller.getAsString("", "user");
		// TODO: implement
	}

	/**
	 * Test for getAsValue method.
	 */
	@Test
	public void getAsValueTest() {
		Object value = controller.getAsValue(null, null, true);
		assertNull(value);

		String actual = "selected user";
		value = controller.getAsValue(actual, null, true);
		assertEquals(value, actual);
	}
}
