package com.sirma.itt.emf.web.menu.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

import freemarker.template.TemplateException;

/**
 * User menu extension point.
 * 
 * @author svelikov
 */
@Named
public class UserMenu implements Plugable {

	public static final String EXTENSION_POINT = "userMenu";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * The Class UserLogout.
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 1000, priority = 1)
	public static class UserLogout extends AbstractPageModel implements PageFragment {

		private static final Logger LOGGER = Logger.getLogger(UserLogout.class);

		private static final String FRAGMENT_NAME = "user-logout";

		private static final String LOGO_TEMPLATE_PATH = "header/mainmenu-right/user-menu/logout";

		private String href;

		@Override
		public String getPath() {
			return "/menu/main/userMenu/logout.xhtml";
		}

		@Override
		public String getPageFragment() {

			Map<String, Object> model = new HashMap<String, Object>();
			model.put("linkName", getLabelProvider().getValue("cmf.usermenu.logout"));
			model.put("name", UserLogout.FRAGMENT_NAME);
			model.put("href", getHRef());
			model.put("id", "emf_" + UserLogout.FRAGMENT_NAME);

			try {
				return getFreemarkerProvider().processTemplateByFullPath(model, LOGO_TEMPLATE_PATH);
			} catch (IOException | TemplateException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(e.getMessage(), e);
				}
			}

			return "";
		}

		/**
		 * Gets the h ref.
		 * 
		 * @return the h ref
		 */
		private String getHRef() {
			if (href == null) {
				href = getServerBaseAndContext() + "/ServiceLogout";
			}
			return href;
		}
	}

	/**
	 * Change password link
	 * @author yasko
	 * 
	 */
	@ApplicationScoped
	@Extension(target = EXTENSION_POINT, enabled = true, order = 999, priority = 1)
	public static class ChangePassword extends AbstractPageModel implements PageFragment {

		private static final Logger LOGGER = Logger.getLogger(ChangePassword.class);
		private static final String CHANGE_PASSWORD_TEMPLATE_PATH = "header/mainmenu-right/user-menu/change-password";

		@Override
		public String getPageFragment() {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("linkText", getLabelProvider().getValue("cmf.usermenu.changePassword"));
			model.put("href", getServerBaseAndContext() + "/user/change-password-form.jsf");

			try {
				return getFreemarkerProvider().processTemplateByFullPath(model,
						CHANGE_PASSWORD_TEMPLATE_PATH);
			} catch (IOException | TemplateException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(e.getMessage(), e);
				}
			}

			return "";
		}

		@Override
		public String getPath() {
			return "/menu/main/userMenu/changePassword.xhtml";
		}
	}
}
