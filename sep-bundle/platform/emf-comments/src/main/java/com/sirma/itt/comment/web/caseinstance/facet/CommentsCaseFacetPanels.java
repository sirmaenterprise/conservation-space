package com.sirma.itt.comment.web.caseinstance.facet;

import javax.inject.Named;

import com.sirma.cmf.web.caseinstance.facet.CaseFacetsExtensionPoint;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class CaseFacetPanels.
 * 
 * @author svelikov
 */
@Named
public class CommentsCaseFacetPanels implements CaseFacetsExtensionPoint {

	/**
	 * The Class CaseCommentsPanel.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class CaseCommentsPanel implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/facet-case-comments.xhtml";
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
