package com.sirma.itt.comment.web.caseinstance.document.facet;

import javax.inject.Named;

import com.sirma.cmf.web.document.facet.DocumentFacet;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class CommentsDocumentFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class CommentsDocumentFacetPanel implements DocumentFacet {

	/**
	 * The Class DefaultDocumentCommentsFacet.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = true, order = 15, priority = 20)
	public static class DefaultDocumentCommentsFacet implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/document/includes/facet-default-document-comments.xhtml";
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
