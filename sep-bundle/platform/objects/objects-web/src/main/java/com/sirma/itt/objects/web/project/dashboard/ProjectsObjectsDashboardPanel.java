package com.sirma.itt.objects.web.project.dashboard;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Register ProjectsObjectsDashboardPanel.
 * 
 * @author cdimitrov
 */
public class ProjectsObjectsDashboardPanel {
	/**
	 * ProjectObjectsPanel extension.
	 */
	@Extension(target = "projectDashboard", enabled = true, order = 60, priority = 1)
	public static class ProjectObjectsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-object-panel.xhtml";
		}
	}

}
