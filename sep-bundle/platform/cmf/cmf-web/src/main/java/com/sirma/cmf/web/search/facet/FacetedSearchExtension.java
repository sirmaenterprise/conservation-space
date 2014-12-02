package com.sirma.cmf.web.search.facet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for search facets.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class FacetedSearchExtension implements Plugable {

	private static final String EXTENSION_POINT = "faceted.search.extension.point";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * CaseSearchFacet extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class CaseSearchFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-case-filters.xhtml";
		}
	}

	/**
	 * TaskSearchFacet extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class TaskSearchFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-task-filters.xhtml";
		}
	}

	/**
	 * MessageSearchFacet extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 30, priority = 1)
	public static class MessageSearchFacet implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/facet-message-filters.xhtml";
		}
	}

	/**
	 * ObjectsExplorerPlugin extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 40, priority = 1)
	public static class ObjectsExplorerPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/search/search-facets/objects-explorer.xhtml";
		}
	}

}
