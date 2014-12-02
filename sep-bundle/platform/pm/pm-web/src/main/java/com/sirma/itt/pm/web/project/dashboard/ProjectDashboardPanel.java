package com.sirma.itt.pm.web.project.dashboard;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered ProjectDashboardPanel panels.
 * 
 * @author svelikov
 */
@Named
public class ProjectDashboardPanel implements ProjectDashboard {

	/**
	 * The Class ProjectDetailsPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class ProjectDetailsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-details-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectMessagesPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 110, priority = 1)
	public static class ProjectMessagesPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-messages-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectTasksPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 40, priority = 1)
	public static class ProjectTasksPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-tasks-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectCasePanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 30, priority = 1)
	public static class ProjectCasePanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-case-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectsDocumentPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 50, priority = 1)
	public static class ProjectsDocumentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-document-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectColleaguesPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 100, priority = 1)
	public static class ProjectColleaguesPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-colleagues-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectMediaPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 70, priority = 1)
	public static class ProjectMediaPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-media-panel.xhtml";
		}
	}

	/**
	 * The Class CommentsPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 80, priority = 1)
	public static class CommentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/comments-panel.xhtml";
		}
	}

	/**
	 * The Class ProjectWorkflowsPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class ProjectWorkflowsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/project/dashboard-panel/project-workflows-panel.xhtml";
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
