package com.sirma.itt.comment.web.workflow.facet;

import com.sirma.cmf.web.workflow.WorkflowFacetPanel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Provides extensions for the WorkflowFacetPanelExtensionPoint.
 * 
 * @author svelikov
 */
public class CommentsWorkflowFacetPanels extends WorkflowFacetPanel {

	/**
	 * The Class CaseCommentsPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 100, priority = 1)
	public static class CaseCommentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/workflow/includes/facet-workflow-comments.xhtml";
		}
	}
}
