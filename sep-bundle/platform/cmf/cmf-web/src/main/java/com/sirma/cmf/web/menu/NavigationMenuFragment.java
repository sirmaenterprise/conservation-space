package com.sirma.cmf.web.menu;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * NavigationMenuFragment extension point.
 * 
 * @author svelikov
 */
public class NavigationMenuFragment implements NavigationMenuPlugin {

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * CMFSearch menu extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 30, priority = 1)
	public static class CMFSearch implements PageFragment {

		@Override
		public String getPath() {
			return "/menu/navigation/cmf-search-menu.xhtml";
		}
	}

	/**
	 * CMFCases menu extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 40, priority = 1)
	public static class CMFCases implements PageFragment {

		@Override
		public String getPath() {
			return "/menu/navigation/cmf-cases-menu.xhtml";
		}
	}

	/**
	 * DmsLink menu extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 50, priority = 1)
	public static class DmsLink implements PageFragment {

		@Override
		public String getPath() {
			return "/menu/navigation/dms-link.xhtml";
		}
	}

}
