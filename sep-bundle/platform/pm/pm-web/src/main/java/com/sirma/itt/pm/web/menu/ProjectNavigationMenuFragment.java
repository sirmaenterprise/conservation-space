package com.sirma.itt.pm.web.menu;

import com.sirma.cmf.web.menu.NavigationMenuPlugin;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Registered navigation menu items in PM module.
 * 
 * @author svelikov
 */
public class ProjectNavigationMenuFragment implements NavigationMenuPlugin {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * The Class ProjectDashboardNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class ProjectDashboardNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/project-dashboard-menu.xhtml";
		}
	}

	/**
	 * The Class ProjectProfileNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 20, priority = 1)
	public static class ProjectProfileNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/project-profile-menu.xhtml";
		}
	}

	/**
	 * The Class ProjectMembersNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 27, priority = 1)
	public static class ProjectMembersNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/project-members-menu.xhtml";
		}
	}

	/**
	 * The Class ProjectRelationsNavigationMenu.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 28, priority = 1)
	public static class ProjectRelationsNavigationMenu implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/project-relations-menu.xhtml";
		}
	}

	/**
	 * The Class CMFSearch.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 30, priority = 2)
	public static class CMFSearch implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/cmf-search-menu.xhtml";
		}
	}

	/**
	 * The Class CMFCases.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 40, priority = 2)
	public static class CMFCases implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/navigation/cmf-cases-menu.xhtml";
		}
	}

}
