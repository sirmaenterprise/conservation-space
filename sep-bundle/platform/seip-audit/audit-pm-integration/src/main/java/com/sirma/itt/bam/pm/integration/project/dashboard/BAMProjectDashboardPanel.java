package com.sirma.itt.bam.pm.integration.project.dashboard;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.pm.web.project.dashboard.ProjectDashboard;

/**
 * Registered ProjectDashboardPanel panels.
 * 
 * @author svelikov
 */
@Named
public class BAMProjectDashboardPanel {

	/**
	 * The Class ProjectActivitiesPanel.
	 */
	@Extension(target = ProjectDashboard.EXTENSION_POINT, enabled = true, order = 90, priority = 1)
	public static class ProjectActivitiesPanel implements PageFragment {
	/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-activities-panel.xhtml";
		}
	}

}
