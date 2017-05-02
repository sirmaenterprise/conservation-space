package com.sirma.cmf.web.resources;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.StylesheetResourceExtensionPoint;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Contributes extesnions to StylesheetResourceExtensionPoint.
 *
 * @author svelikov
 */
@Extension(target = StylesheetResourceExtensionPoint.EXTENSION_POINT, enabled = true, order = 10, priority = 1)
public class CmfCSSResource implements PageFragment {

	@Override
	public String getPath() {
		return "/common/cmf-stylesheets.xhtml";
	}
}