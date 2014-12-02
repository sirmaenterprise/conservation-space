package com.sirma.itt.objects.web.userdashboard.panel;

import com.sirma.cmf.web.userdashboard.UserDashboard;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Register ObjectsUserDashboardPanel.
 * 
 * @author cdimitrov
 */
public class ObjectsUserDashboardPanel {

	/**
	 * MyObjectsPanelExtension.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 50, priority = 1)
	public static class MyObjectsPanelExtension implements PageFragment {

		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/objects-panel.xhtml";
		}
	}

	/**
	 * MyWorkflowsPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class MyWorkflowsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/workflows-panel.xhtml";
		}
	}

}
