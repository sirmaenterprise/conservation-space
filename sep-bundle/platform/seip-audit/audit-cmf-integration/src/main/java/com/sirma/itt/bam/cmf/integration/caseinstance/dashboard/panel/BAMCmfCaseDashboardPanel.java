package com.sirma.itt.bam.cmf.integration.caseinstance.dashboard.panel;

import com.sirma.cmf.web.caseinstance.dashboard.CaseDashboard;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered BAMCmfCaseDashboard panels.
 * 
 * @author cdimitrov
 */
public class BAMCmfCaseDashboardPanel {

	/**
	 * The Class CaseActivityPanel.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 80, priority = 1)
	public static class CaseActivitiesPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-activities-panel.xhtml";
		}
	}

}
