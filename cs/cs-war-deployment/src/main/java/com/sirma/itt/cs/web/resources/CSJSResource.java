package com.sirma.itt.cs.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * CS deployment module javascript files.
 * 
 * @author svelikov
 */
public class CSJSResource implements JavascriptResourceExtensionPoint {

	/**
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 205, priority = 1)
	public static class CSJavascriptPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/resources/common/cs-scripts.xhtml";
		}
	}
}