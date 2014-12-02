package com.sirma.itt.bam.pm.integration.web.userdashboard.panel;

import com.sirma.cmf.web.userdashboard.UserDashboard;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered PMUserDashboardPanel panels.
 * 
 * @author svelikov
 */
public class BAMPmUserDashboardPanel {
	
	/**
	 * The Class MyActivitiesPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 102, priority = 1)
	public static class MyActivitiesPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/activities-panel.xhtml";
		}
	}

}
