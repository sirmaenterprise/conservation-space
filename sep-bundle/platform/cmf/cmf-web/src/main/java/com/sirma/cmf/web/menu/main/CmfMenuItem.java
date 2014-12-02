package com.sirma.cmf.web.menu.main;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.web.menu.main.MainMenu;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.PageModel;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Contributes to the main menu extension point.
 * 
 * @author svelikov
 */
public class CmfMenuItem extends MainMenu {

	protected static final String HEADER_MAINMENU_PREFIX_PATH = "header/mainmenu/";

	/**
	 * MyDashboard extension.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 300, priority = 1)
	public static class MyDashboards extends AbstractPageModel implements PageFragment, Plugable {

		public static final String FRAGMENT_NAME = "my-dashboards";

		private static final String MY_DASHBOARDS_TEMPLATE_PATH = HEADER_MAINMENU_PREFIX_PATH
				+ FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		/** The Constant EXTENSION_POINT. */
		public static final String EXTENSION_POINT = "myDashboardsMenu";

		/** The extension. */
		@Inject
		@ExtensionPoint(value = EXTENSION_POINT)
		private Iterable<PageModel> extension;

		@Override
		public String getPath() {
			return "/menu/main/my-dashboard.xhtml";
		}

		@Override
		public String getExtensionPoint() {
			return EXTENSION_POINT;
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/userDashboard/dashboard.jsf",
							"cmf.main.menu.userdashboard", null, null, null),
					MY_DASHBOARDS_TEMPLATE_PATH);
		}
	}
}
