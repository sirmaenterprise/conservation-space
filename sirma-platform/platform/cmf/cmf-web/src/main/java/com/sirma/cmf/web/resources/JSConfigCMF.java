package com.sirma.cmf.web.resources;

import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JSConfigExtensionPoint;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extensions for js.config extension point.
 *
 * @author svelikov
 */
public class JSConfigCMF implements JSConfigExtensionPoint {

	/**
	 * Extension for js.config that provides additional configurations.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class CMFJSConfigPlugin implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/js-config-cmf.xhtml";
		}
	}
}