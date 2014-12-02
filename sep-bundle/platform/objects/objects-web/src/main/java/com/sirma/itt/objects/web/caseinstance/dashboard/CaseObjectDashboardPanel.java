package com.sirma.itt.objects.web.caseinstance.dashboard;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Register CaseObjectDashboardPanel.
 * 
 * @author cdimitrov
 */
public class CaseObjectDashboardPanel {

	/**
	 * CaseObjectsPanel extension.
	 */
	@Extension(target = "caseDashboard", enabled = true, order = 50, priority = 1)
	public static class CaseObjectsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-object-panel.xhtml";
		}
	}

	/**
	 * CaseWorkflowsPanel extension.
	 */
	@Extension(target = "caseDashboard", enabled = true, order = 40, priority = 2)
	public static class CaseWorkflowsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-workflow-panel.xhtml";
		}
	}

}
