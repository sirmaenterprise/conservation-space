package com.sirma.cmf.web.menu.main;

import com.sirma.itt.emf.web.menu.main.MainMenu;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Code list server main menu extension.
 *
 * @author Vilizar Tsonev
 */
public class CodeListServerItem extends MainMenu {

	/**
	 * Code list navigation fragment.
	 */
	@Extension(target = AdminMenuExtension.EXTENSION_POINT, enabled = true, order = 10001, priority = 1)
	public static class CodeListServerNavigationFragment extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "codelist-server";

		private static final String CODELIST_SERVER_TEMPLATE_PATH = "header/mainmenu/"
				+ AdminMenuExtension.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			// TODO do we need this?
			return "/menu/navigation/codelist-server-navigation-menu.xhtml";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/clsearch.jsf", "emf.bam.codelistserver", null, null, null),
					CODELIST_SERVER_TEMPLATE_PATH);
		}
	}
}
