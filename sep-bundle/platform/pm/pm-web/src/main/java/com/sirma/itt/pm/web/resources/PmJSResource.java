package com.sirma.itt.pm.web.resources;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * PM module javascript files.
 * 
 * @author svelikov
 */
@Named
public class PmJSResource implements JavascriptResourceExtensionPoint {

	/**
	 * The Class main javascript file for the PM module.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class PMJavascriptPlugin implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/pm-scripts.xhtml";
		}
	}

}