package com.sirma.itt.pm.web.project.facet;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * ProjectFacetPanels extension point.
 * 
 * @author svelikov
 */
@Named
public class ProjectFacetPanels implements ProjectFacetsExtensionPoint {

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * ProjectRelatedContentFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 20, priority = 1)
	public static class ProjectRelatedContentPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/project/includes/facet-related-content.xhtml";
		}
	}

	/**
	 * ProjectCommentsFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 30, priority = 1)
	public static class ProjectCommentsPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/project/includes/facet-project-comments.xhtml";
		}
	}

	/**
	 * ProjectMembersFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 40, priority = 1)
	public static class ProjectMembersPanel implements PageFragment {

		@Override
		public String getPath() {
			return "/project/includes/facet-project-members.xhtml";
		}
	}

}
