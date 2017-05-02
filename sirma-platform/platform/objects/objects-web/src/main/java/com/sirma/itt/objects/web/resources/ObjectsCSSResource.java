package com.sirma.itt.objects.web.resources;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.StylesheetResourceExtensionPoint;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Objects module stylesheet files.
 *
 * @author svelikov
 */
@Extension(target = StylesheetResourceExtensionPoint.EXTENSION_POINT, enabled = true, order = 60, priority = 1)
public class ObjectsCSSResource implements PageFragment {

	@Override
	public String getPath() {
		return "/common/objects-stylesheets.xhtml";
	}
}