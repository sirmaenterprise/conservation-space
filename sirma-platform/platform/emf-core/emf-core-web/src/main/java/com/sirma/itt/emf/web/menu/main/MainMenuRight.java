package com.sirma.itt.emf.web.menu.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.security.User;

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
			Map<String, Object> model = new HashMap<>(4);
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
			Map<String, Object> model = new HashMap<>();
			User currentUser = getCurrentUser();
			model.put("currentUserName", currentUser.getDisplayName());
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
			Map<String, Object> model = new HashMap<>();
			model.put("placeholder", getLabelProvider().getValue("search.quick.placeholder"));
			return "";
		}
	}

	/**
	 * Main menu upload button.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 700, priority = 1)
	public static class MainMenuUpload extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "main-menu-upload";

		private static final String UPLOAD_TEMPLATE_PATH = "header/mainmenu-right/upload/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public String getPageFragment() {
			// as of CMF-20403 this is not needed.
			return "";
		}
	}

	/**
	 * The Class create new button - teleport to UI-2.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 600, priority = 1)
	public static class CreateNewButton extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "create-new";

		private static final String CREATE_NEW_TEMPLATE_PATH = "header/mainmenu-right/" + FRAGMENT_NAME + "/"
				+ FRAGMENT_NAME;

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public String getPageFragment() {
			// as of CMF-20403 this is not needed.
			return "";
		}
	}

}
