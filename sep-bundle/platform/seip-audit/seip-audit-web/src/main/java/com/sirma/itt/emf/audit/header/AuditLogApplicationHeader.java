package com.sirma.itt.emf.audit.header;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.header.EmfApplicationHeader;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Emf extension for the application header.
 * @author Nikolay Velkov
 *
 */
public class AuditLogApplicationHeader extends EmfApplicationHeader {
	/**
	 * The Class ProjectTitle.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 11, priority = 3)
	public static class TitleHeader implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/header/audit-log-header.xhtml";
		}
	}
}
