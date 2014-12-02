/*
 * 
 */
package com.sirma.itt.comment.web.workflow.task.facet;

import javax.inject.Named;

import com.sirma.cmf.web.workflow.task.facet.WorkflowTaskFacetPanel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class WorkflowTaskFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class CommentsWorkflowTaskFacetPanel extends WorkflowTaskFacetPanel {

	/**
	 * The Class WorkflowTaskCommentsFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class WorkflowTaskCommentsFacet implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/task/workflow/includes/facet-task-comments.xhtml";
		}
	}

}
