package com.sirma.itt.pm.web.userdashboard.panel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.pm.PMTest;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.web.userdashboard.panel.filters.PMUserDashboardProjectFilter;

/**
 * Test class for {@link MyProjectsPanel}.
 * 
 * @author cdimitrov
 */

@Test
public class MyProjectsPanelTest extends PMTest {

	/** The project panel reference. */
	private final MyProjectsPanel myProjectsPanel;

	/** The project instance that will be used in the tests. */
	private final ProjectInstance projectInstance;

	private final LabelProvider labelProvider;

	/**
	 * Default constructor.
	 */
	public MyProjectsPanelTest() {

		// create project instance for the test class.
		projectInstance = createProjectInstance(Long.valueOf(1), "dmsId");

		myProjectsPanel = new MyProjectsPanel() {

			private static final long serialVersionUID = -3378182947622659867L;

		};

		labelProvider = mock(LabelProvider.class);

		when(
				labelProvider
						.getValue(PMUserDashboardProjectFilter.PM_PROJECT_USER_DASHBOARD_PROJECTS_FILTER_PREF
								+ "all_projects")).thenReturn("all-projects");

		when(
				labelProvider
						.getValue(PMUserDashboardProjectFilter.PM_PROJECT_USER_DASHBOARD_PROJECTS_FILTER_PREF
								+ "active_projects")).thenReturn("active-projects");

		when(
				labelProvider
						.getValue(PMUserDashboardProjectFilter.PM_PROJECT_USER_DASHBOARD_PROJECTS_FILTER_PREF
								+ "completed_projects")).thenReturn("completed-projects");

		ReflectionUtils.setField(myProjectsPanel, "labelProvider", labelProvider);

	}

}
