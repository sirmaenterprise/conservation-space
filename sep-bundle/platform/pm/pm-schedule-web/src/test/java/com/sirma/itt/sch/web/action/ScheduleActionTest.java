package com.sirma.itt.sch.web.action;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.sch.web.PMSchTest;
import com.sirma.itt.sch.web.mock.service.ScheduleServiceMock;

/**
 * The Class ScheduleActionTest.
 * 
 * @author svelikov
 */
@Test
public class ScheduleActionTest extends PMSchTest {

	/** The action. */
	private ScheduleAction action;

	/**
	 * Instantiates a new schedule action test.
	 */
	public ScheduleActionTest() {
		action = new ScheduleAction() {
			private static final long serialVersionUID = 636540130734609723L;

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}
		};
		ReflectionUtils.setField(action, "log", log);
		ReflectionUtils.setField(action, "scheduleService", new ScheduleServiceMock());
	}

	/**
	 * Open project schedule test.
	 */
	public void openProjectScheduleTest() {

		ScheduleInstance scheduleInstance = null;

		// test if no project instance is set in context - should return and schedule must not be
		// created
		action.openProjectSchedule(createNavigationMenuEvent("navigation", "project-schedule"));
		scheduleInstance = action.getDocumentContext().getInstance(ScheduleInstance.class);
		Assert.assertNull(scheduleInstance);
		scheduleInstance = null;

		// test if project instance has no id - should return and schedule must not be created
		ProjectInstance projectInstance = new ProjectInstance();
		action.getDocumentContext().addInstance(projectInstance);
		action.openProjectSchedule(createNavigationMenuEvent("navigation", "project-schedule"));
		scheduleInstance = action.getDocumentContext().getInstance(ScheduleInstance.class);
		Assert.assertNull(scheduleInstance);
		scheduleInstance = null;

		// test with existing project instance with assigned db id
		projectInstance.setId(1l);
		action.getDocumentContext().addInstance(projectInstance);
		action.openProjectSchedule(createNavigationMenuEvent("navigation", "project-schedule"));
		scheduleInstance = action.getDocumentContext().getInstance(ScheduleInstance.class);
		Assert.assertNotNull(scheduleInstance);
	}
}
