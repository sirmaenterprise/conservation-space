package com.sirma.itt.cs.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.StylesheetResourceExtensionPoint;

/**
 * CS deployment module stylesheet files.
 * 
 * @author svelikov
 */
public class CSCSSResource implements StylesheetResourceExtensionPoint {

	/**
	 * CSStylesheetsPlugin.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 1000, priority = 1)
	public static class CSStylesheetsPlugin implements PageFragment {

		@Override
		public String getPath() {
			return "/resources/common/cs-stylesheets.xhtml";
		}
	}
}