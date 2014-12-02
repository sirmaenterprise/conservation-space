package com.sirma.itt.comment.web.caseinstance.dashboard;

import javax.inject.Named;

import com.sirma.cmf.web.caseinstance.dashboard.CaseDashboard;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * @author svelikov
 */
@Named
public class CommentsCaseDashboardPanel implements CaseDashboard {

	/**
	 * The Class CommentsPanel.
	 */
	@Extension(target = CaseDashboard.EXTENSION_POINT, enabled = true, order = 70, priority = 1)
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
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
