package com.sirma.itt.emf.web.menu.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Registered right main menu item plugins.
 * 
 * @author svelikov
 */
@Named
public class MainMenuRight implements Plugable {

	public static final String EXTENSION_POINT = "main.menu.right";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * User help menu.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 900, priority = 1)
	public static class UserHelp extends AbstractPageModel implements PageFragment {

		private static final String TEMPLATE_USER_HELP = "header/mainmenu-right/help-menu/user-help";

		/** The extension. */
		@Inject
		@ExtensionPoint(value = UserHelpMenu.EXTENSION_POINT)
		private Iterable<Plugin> userHelpMenuExtentionsPoints;

		@Override
		public String getPath() {
			return "/menu/main/userHelp/user-help-menu.xhtml";
		}

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("name", getLabelProvider().getValue("emf.userhelp"));
			Iterator<Plugin> iterator = userHelpMenuExtentionsPoints.iterator();
			model.put("submenus", loadHtmlPageFragments(iterator));
			return buildTemplate(model, TEMPLATE_USER_HELP);
		}
	}

	/**
	 * The Class UserMenu.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 1000, priority = 1)
	public static class UserMenu extends AbstractPageModel implements PageFragment {

		private static final String TEMPLATE_USER_MENU = "header/mainmenu-right/user-menu/user-menu";

		/** The extension. */
		@Inject
		@ExtensionPoint(value = com.sirma.itt.emf.web.menu.main.UserMenu.EXTENSION_POINT)
		private Iterable<Plugin> userMenuExtentionsPoints;

		@Override
		public String getPath() {
			return "/menu/main/userMenu/user-menu.xhtml";
		}

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			User currentUser = getAuthenticationService().getCurrentUser();
			if (currentUser != null) {
				model.put("currentUserName", currentUser.getDisplayName());
			} else {
				model.put("currentUserName", "Not Implemented");
			}
			Iterator<Plugin> iterator = userMenuExtentionsPoints.iterator();
			model.put("submenus", loadHtmlPageFragments(iterator));

			return buildTemplate(model, TEMPLATE_USER_MENU);
		}
	}

	/**
	 * MainMenuSeach.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 800, priority = 1)
	public static class MainMenuSeach extends AbstractPageModel implements PageFragment {

		private static final String QUICK_SEARCH_MENU_TEMPLATE = "header/mainmenu-right/search/main-menu-search";

		@Override
		public String getPath() {
			return "/menu/main/userMenu/main-menu-search.xhtml";
		}

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("placeholder", getLabelProvider().getValue("search.quick.placeholder"));
			String buildTemplate = buildTemplate(model, QUICK_SEARCH_MENU_TEMPLATE);
			return buildTemplate;
		}
	}

}
