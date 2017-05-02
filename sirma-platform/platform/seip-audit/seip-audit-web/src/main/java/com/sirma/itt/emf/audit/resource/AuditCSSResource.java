package com.sirma.itt.emf.audit.resource;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.StylesheetResourceExtensionPoint;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Contributes extensions to StylesheetResourceExtensionPoint.
 *
 * @author Hristo Lungov
 */
@Extension(target = StylesheetResourceExtensionPoint.EXTENSION_POINT, enabled = true, order = 90, priority = 1)
public class AuditCSSResource implements PageFragment {

	@Override
	public String getPath() {
		return "/common/audit-stylesheets.xhtml";
	}
}