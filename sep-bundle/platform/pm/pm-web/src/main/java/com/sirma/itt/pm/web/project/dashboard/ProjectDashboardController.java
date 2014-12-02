package com.sirma.itt.pm.web.project.dashboard;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.userdashboard.DashboardController;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * Backing bean for project dashboards initializing functionality.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class ProjectDashboardController extends DashboardController {

	private static final long serialVersionUID = 4883302135917288931L;

	@Inject
	@InstanceType(type = "ProjectDashboard")
	private Instance<DashboardPanelController> projectDashboardPanelControllers;

	/**
	 * Inits the project dashboard.
	 */
	public void initProjectDashboard() {
		TimeTracker timer = null;
		if (log.isDebugEnabled()) {
			timer = TimeTracker.createAndStart();
			log.debug("Initializing project dashboard");
		}
		invokeControllersInit(projectDashboardPanelControllers);
		if (timer != null) {
			log.debug("Project dashboard initialization took " + timer.stopInSeconds() + " s");
		}
	}
}
