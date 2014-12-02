package com.sirma.itt.comment.web.userdashboard;

import javax.inject.Named;

import com.sirma.cmf.web.userdashboard.UserDashboard;
import com.sirma.cmf.web.userdashboard.UserDashboardPanel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class CommentsUserDashboardPanel.
 * 
 * @author svelikov
 */
@Named
public class CommentsUserDashboardPanel extends UserDashboardPanel {

	/**
	 * The Class CommentsPanel.
	 */
	@Extension(target = UserDashboard.EXTENSION_POINT, enabled = true, order = 70, priority = 1)
	public static class CommentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/comments-panel.xhtml";
		}
	}

}
