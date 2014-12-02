package com.sirma.itt.idoc.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JSConfigExtensionPoint;

/**
 * Idoc javascript config plugin.
 * 
 * @author yasko
 * 
 */
@Extension(target = JSConfigExtensionPoint.EXTENSION_POINT, order = 40, priority = 1)
public class IdocJsConfig implements JSConfigExtensionPoint, PageFragment {

	@Override
	public String getPath() {
		return "/common/idoc-js-config.xhtml";
	}

}
