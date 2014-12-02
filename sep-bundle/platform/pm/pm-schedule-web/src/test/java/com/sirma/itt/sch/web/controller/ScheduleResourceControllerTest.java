package com.sirma.itt.sch.web.controller;

import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.sch.web.PMSchTest;
import com.sirma.itt.sch.web.mock.service.ProjectServiceMock;
import com.sirma.itt.sch.web.mock.service.ScheduleResourceServiceMock;

/**
 * The Class ScheduleResourceControllerTest.
 * 
 * @author svelikov
 */
@Test
public class ScheduleResourceControllerTest extends PMSchTest {

	/** The controller. */
	private ScheduleResourceController controller;

	/**
	 * Instantiates a new schedule resource controller test.
	 */
	public ScheduleResourceControllerTest() {
		controller = new ScheduleResourceController() {

			@Override
			protected ScheduleInstance getScheduleInstance(ProjectInstance projectInstance) {
				ScheduleInstance scheduleInstance = new ScheduleInstance();
				return scheduleInstance;
			}
		};

		ReflectionUtils.setField(controller, "log", log);
		ReflectionUtils.setField(controller, "debug", true);
		ReflectionUtils.setField(controller, "projectService", new ProjectServiceMock());
		ReflectionUtils.setField(controller, "scheduleResourceService",
				new ScheduleResourceServiceMock());
		// ReflectionUtils.setField(controller, "typeConvertor", new
		// ScheduleEntryToJsonConverter());
	}

	// /**
	// * Load dependencies test.
	// */
	// public void loadDependenciesTest() {
	// Assert.fail("Not implemented!");
	// }
	//
	// /**
	// * Load assignments test.
	// */
	// public void loadAssignmentsTest() {
	// Assert.fail("Not implemented!");
	// }

}
