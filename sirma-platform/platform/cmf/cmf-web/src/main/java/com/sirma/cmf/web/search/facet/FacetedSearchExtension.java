package com.sirma.cmf.web.search.facet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension point for search facets.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class FacetedSearchExtension implements Plugable {

	public static final String EXTENSION_POINT = "faceted.search.extension.point";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * CaseSearchFacet extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class LibraryFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-document-library.xhtml";
		}
	}

	/**
	 * MessageSearchFacet extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 40, priority = 1)
	public static class MessageSearchFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-message-filters.xhtml";
		}
	}

	/**
	 * ObjectsExplorerPlugin extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 50, priority = 1)
	public static class ObjectsExplorerPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/objects-explorer.xhtml";
		}
	}

}
