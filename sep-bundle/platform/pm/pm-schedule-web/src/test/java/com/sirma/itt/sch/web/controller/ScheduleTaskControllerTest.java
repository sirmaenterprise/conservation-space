package com.sirma.itt.sch.web.controller;

import java.io.Serializable;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.sch.web.PMSchTest;

/**
 * The Class ScheduleTaskControllerTest.
 * 
 * @author svelikov
 */
@Test
public class ScheduleTaskControllerTest extends PMSchTest {

	/** The Constant RESPONSE2. */
	private static final String RESPONSE2 = "response";

	/** The controller. */
	private ScheduleTaskController controller;

	/**
	 * Instantiates a new schedule task controller test.
	 */
	public ScheduleTaskControllerTest() {
		controller = new ScheduleTaskController() {
			@Override
			protected String loadAll(String node, Serializable projectDbId) throws JSONException {
				if (node == null || projectDbId == null) {
					return null;
				}
				return RESPONSE2;
			}
		};
		ReflectionUtils.setField(controller, "log", log);
		ReflectionUtils.setField(controller, "debug", true);

		// scheduleService = mock(ScheduleServiceImpl.class);
	}

	/**
	 * View test.
	 */
	public void viewTest() {
		try {
			String response = controller.view("1", "1");
			Assert.assertEquals(response, RESPONSE2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// /**
	// * Commit test.
	// */
	// public void commitTest() {
	// Assert.fail("Not implemented!");
	// }
	//
	// /**
	// * Update test.
	// */
	// public void updateTest() {
	// Assert.fail("Not implemented!");
	// }
	//
	// /**
	// * Delete test.
	// */
	// public void deleteTest() {
	// Assert.fail("Not implemented!");
	// }
	//
	// /**
	// * Cancel test.
	// */
	// public void cancelTest() {
	// Assert.fail("Not implemented!");
	// }
	//
	// /**
	// * Gets the allowed children test.
	// */
	// public void getAllowedChildrenTest() {
	// Assert.fail("Not implemented!");
	// }

}
