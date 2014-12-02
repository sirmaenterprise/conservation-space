/*
 * 
 */
package com.sirma.itt.comment.web.standaloneTask.facet;

import javax.inject.Named;

import com.sirma.cmf.web.standaloneTask.facet.StandaloneTaskFacetPanel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class StandaloneTaskFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class CommentsStandaloneTaskFacetPanel extends StandaloneTaskFacetPanel {

	/**
	 * The Class StandaloneTaskCommentsFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class StandaloneTaskCommentsFacet implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/task/standaloneTask/includes/facet-task-comments.xhtml";
		}
	}

}
