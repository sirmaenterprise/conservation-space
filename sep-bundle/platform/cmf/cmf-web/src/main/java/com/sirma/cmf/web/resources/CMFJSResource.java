package com.sirma.cmf.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * CMF module javascript files.
 * 
 * @author svelikov
 */
public class CMFJSResource implements JavascriptResourceExtensionPoint {

	/**
	 * CMFJavascriptPlugin extesnion.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class CMFJavascriptPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/common/cmf-scripts.xhtml";
		}
	}
}