package com.sirma.itt.menu.navigation;

import com.sirma.cmf.web.menu.NavigationMenuFragment;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * CSNavigationMenuFragment extension point. Disables some of the plugins.
 * 
 * @author svelikov
 */
public class CSNavigationMenuFragment extends NavigationMenuFragment {

	/**
	 * CMFSearch menu extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 30, priority = 3)
	public static class CMFSearchDisabled implements PageFragment {

		@Override
		public String getPath() {
			return "/menu/navigation/cmf-search-menu.xhtml";
		}
	}

}
