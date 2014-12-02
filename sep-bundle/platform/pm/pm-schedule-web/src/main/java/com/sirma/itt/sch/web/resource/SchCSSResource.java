package com.sirma.itt.sch.web.resource;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.StylesheetResourceExtensionPoint;

/**
 * Schedule module stylesheet files.
 * 
 * @author svelikov
 */
@Extension(target = StylesheetResourceExtensionPoint.EXTENSION_POINT, enabled = true, order = 40, priority = 1)
public class SchCSSResource implements PageFragment {

	@Override
	public String getPath() {
		return "/common/schedule-stylesheets.xhtml";
	}
}