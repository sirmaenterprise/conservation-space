package com.sirma.cmf.web.document.facet;

import javax.inject.Named;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

/**
 * DocumentFacet panels extension point.
 *
 * @author svelikov
 */
@Named
public class DocumentFacetPanel implements DocumentFacet {

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * DocumentRevisionsFacet extension.
	 */
	@Extension(target = DocumentFacet.EXTENSION_POINT, enabled = false, order = 90, priority = 1)
	public static class DocumentRevisionsFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/document/includes/facet-document-revisions.xhtml";
		}

	}

}