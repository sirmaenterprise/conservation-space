package com.sirma.itt.pm.web.userdashboard.panel;

import com.sirma.cmf.web.userdashboard.UserDashboard;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered PMUserDashboardPanel panels.
 * 
 * @author svelikov
 */
public class PMUserDashboardPanel {

	/**
	 * The Class MyProjectsPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 5, priority = 1)
	public static class MyProjectsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/projects-panel.xhtml";
		}
	}

}
