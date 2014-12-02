package com.sirma.cmf.web.caseinstance.dashboard;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered CaseDashboardPanel panels.
 * 
 * @author svelikov
 */
@Named
public class CaseDashboardPanel implements CaseDashboard {

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * CaseDetailsPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class CaseDetailsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-details-panel.xhtml";
		}
	}

	/**
	 * CaseDocumentsPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 30, priority = 1)
	public static class CaseDocumentsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-documents-panel.xhtml";
		}
	}

	/**
	 * CaseMessagesPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 100, priority = 1)
	public static class CaseMessagesPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-messages-panel.xhtml";
		}
	}

	/**
	 * TasksPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class TasksPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-tasks-panel.xhtml";
		}
	}

	/**
	 * CaseLinksPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = false, order = 110, priority = 1)
	public static class CaseLinksPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-links-panel.xhtml";
		}
	}

	/**
	 * CaseColleaguesPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 90, priority = 1)
	public static class CaseColleaguesPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-colleagues-panel.xhtml";
		}
	}

	/**
	 * CaseMediaPanel extension.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 60, priority = 1)
	public static class CaseMediaPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/case/dashboard-panel/case-media-panel.xhtml";
		}
	}

}
