package com.sirma.cmf.web.menu.main;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.menu.main.UserMenu;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * CmfUserMenuItem extension point.
 * 
 * @author svelikov
 */
@Named
public class CmfUserMenuItem extends UserMenu {

	/**
	 * UserPreferences extension.
	 */
	@Extension(target = EXTENSION_POINT, enabled = false, order = 10, priority = 1)
	public static class UserPreferences implements PageFragment {

		@Override
		public String getPath() {
			return "/menu/main/userMenu/preferences.xhtml";
		}
	}

}
