package com.sirma.itt.objects.web.resources;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JSConfigExtensionPoint;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Js config extension for objects module
 *
 * @author yasko
 */
@Extension(target = JSConfigExtensionPoint.EXTENSION_POINT, enabled = true, order = 20, priority = 1)
public class JsConfigObjects implements JSConfigExtensionPoint, PageFragment {

	@Override
	public String getPath() {
		return "/common/js-config-objects.xhtml";
	}
}
