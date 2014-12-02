package com.sirma.itt.emf.audit.web.menu;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.menu.main.MainMenu;
import com.sirma.itt.emf.web.plugin.AbstractPageModel;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Audit Log main menu extension.
 * 
 * @author Iskren Borisov
 */
public class AuditLogItem extends MainMenu {

	/**
	 * Audit Log navigation fragment.
	 */
	@Extension(target = AdminMenuExtension.EXTENSION_POINT, enabled = true, order = 10000, priority = 1)
	public static class AuditLogNavigationFragment extends AbstractPageModel implements
			PageFragment {

		private static final String FRAGMENT_NAME = "audit-log";

		private static final String AUDIT_LOG_TEMPLATE_PATH = "header/mainmenu/"
				+ AdminMenuExtension.FRAGMENT_NAME + "/" + FRAGMENT_NAME;

		@Override
		public String getPath() {
			return "/menu/navigation/auditlog-navigation-menu.xhtml";
		}

		@Override
		public String getPageFragment() {
			return buildTemplate(
					createModel(FRAGMENT_NAME, "/audit/audit-log.jsf", "emf.bam.auditlog", null,
							null, null), AUDIT_LOG_TEMPLATE_PATH);
		}
	}
}
