package com.sirma.itt.emf.web.menu.main;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * User help menu extension point.
 * 
 * @author svelikov
 */
@Named
public class UserHelpMenu implements Plugable {

	public static final String EXTENSION_POINT = "user.help";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * User guide menu extension.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 800, priority = 1)
	public static class UserGuide extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "user-guide";

		private static final String TEMPLATE_USER_GUIDE = "header/mainmenu-right/help-menu/user-guide";

		private String href;

		@Override
		public String getPath() {
			return "/menu/main/userHelp/user-guide.xhtml";
		}

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("linkLabel", getLabelProvider().getValue("emf.usermenu.help"));
			model.put("linkName", FRAGMENT_NAME);
			model.put("href", getHRref());
			model.put("id", "emf_" + FRAGMENT_NAME);
			return buildTemplate(model, TEMPLATE_USER_GUIDE);
		}

		/**
		 * Gets the h rref.
		 * 
		 * @return the h rref
		 */
		private String getHRref() {
			if (href == null) {
				href = getEmfServerBaseUrl() + "/userhelp";
			}
			return href;
		}
	}

	/**
	 * Help request menu extension.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 900, priority = 1)
	public static class HelpRequest extends AbstractPageModel implements PageFragment {

		private static final String FRAGMENT_NAME = "help-request";

		private static final String TEMPLATE_HELP_REQUEST = "header/mainmenu-right/help-menu/help-request";

		private String href;

		@Override
		public String getPath() {
			return "/menu/main/userHelp/help-request.xhtml";
		}

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("linkLabel", getLabelProvider().getValue("cmf.usermenu.helpRequest"));
			model.put("linkName", FRAGMENT_NAME);
			model.put("id", "emf_" + FRAGMENT_NAME);
			model.put("href", getHRref());
			return buildTemplate(model, TEMPLATE_HELP_REQUEST);
		}

		/**
		 * Gets the h rref.
		 * 
		 * @return the h rref
		 */
		private String getHRref() {
			if (href == null) {
				href = getServerBaseAndContext() + "/help/help-request.jsf";
			}
			return href;
		}
	}

}
