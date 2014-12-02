package com.sirma.itt.objects.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * Objects module javascript files.
 * 
 * @author svelikov
 */
public class ObjectsJSResource implements JavascriptResourceExtensionPoint {

	/**
	 * ObjectsJavascriptPlugin.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 70, priority = 1)
	public static class ObjectsJavascriptPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/common/objects-scripts.xhtml";
		}
	}
}