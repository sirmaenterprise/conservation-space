package com.sirma.itt.cs.search.facet;

import com.sirma.cmf.web.search.facet.FacetedSearchExtension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Overrides some of the plugins in order to disbale them.
 *
 * @author akunchev
 */
public class CSFacetedSearchExtension extends FacetedSearchExtension {

	// This is temp, until build version for CS is up to 1.10.0
	private static final String EXTENSION_POINT = "faceted.search.extension.point";

	/**
	 * Disables document(project) library.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 10, priority = 10)
	public static class LibraryFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-document-library.xhtml";
		}
	}

}
