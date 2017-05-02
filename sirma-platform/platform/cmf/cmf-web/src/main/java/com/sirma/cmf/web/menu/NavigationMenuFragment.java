package com.sirma.cmf.web.menu;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

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

}
