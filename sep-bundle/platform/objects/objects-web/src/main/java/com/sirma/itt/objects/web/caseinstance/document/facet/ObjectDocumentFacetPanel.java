package com.sirma.itt.objects.web.caseinstance.document.facet;

import javax.inject.Named;

import com.sirma.cmf.web.document.facet.DocumentFacet;
import com.sirma.cmf.web.document.facet.DocumentFacetPanel;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class ObjectDocumentFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class ObjectDocumentFacetPanel extends DocumentFacetPanel {

	/**
	 * The Class RelationsFacet.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = true, order = 25, priority = 1)
	public static class RelationsFacet implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/document/includes/facet-relations.xhtml";
		}
	}

}
