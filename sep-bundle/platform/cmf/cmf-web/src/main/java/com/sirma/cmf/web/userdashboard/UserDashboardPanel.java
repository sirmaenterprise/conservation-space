package com.sirma.cmf.web.userdashboard;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered UserDashboardPanel panels.
 * 
 * @author svelikov
 */
@Named
public class UserDashboardPanel implements UserDashboard {

	/**
	 * The Class MyCasesPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class MyCasesPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/cases-panel.xhtml";
		}
	}

	/**
	 * The Class MyDocumentsPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 40, priority = 1)
	public static class MyDocumentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/documents-panel.xhtml";
		}
	}

	/**
	 * The Class MyTasksPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 30, priority = 1)
	public static class MyTasksPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/tasks-panel.xhtml";
		}
	}

	/**
	 * The Class MyMediaPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 60, priority = 1)
	public static class MyMediaPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/userDashboard/dashboard-panel/media-panel.xhtml";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
