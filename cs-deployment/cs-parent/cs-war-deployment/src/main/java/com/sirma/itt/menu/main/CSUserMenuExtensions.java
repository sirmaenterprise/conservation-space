package com.sirma.itt.menu.main;

import com.sirma.itt.emf.web.menu.main.UserMenu;

/**
 * CSUserMenuExtensions. Override some of the plugins in order to disbale them.
 * 
 * @author svelikov
 */
public class CSUserMenuExtensions extends UserMenu {

	// /**
	// * Disable change password menu.
	// */
	// @ApplicationScoped
	// @Extension(target = EXTENSION_POINT, enabled = false, order = 999, priority = 2)
	// public static class ChangePasswordDisabled extends AbstractPageModel implements PageFragment
	// {
	//
	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public String getPageFragment() {
	// return "";
	// }
	//
	// /**
	// * {@inheritDoc}
	// */
	// @Override
	// public String getPath() {
	// return "";
	// }
	// }
}
