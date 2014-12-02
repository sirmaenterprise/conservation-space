package com.sirma.itt.sch.web.resource;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * Schedule module javascript files.
 * 
 * @author svelikov
 */
public class SchJSResource implements JavascriptResourceExtensionPoint {

	/**
	 * The Class ScheduleJavascriptPlugin.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 40, priority = 1)
	public static class ScheduleJavascriptPlugin implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/schedule-scripts.xhtml";
		}
	}
}