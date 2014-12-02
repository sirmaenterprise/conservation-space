package com.sirma.cmf.web.browser.tabs;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.browser.tabs.BrowserTabs;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * This class will holds extension for browser tab. Based on modules that are loaded will switch
 * functionality for generating tab title and icon.
 * 
 * @author cdimitrov
 */
public class MainBrowserTabsExtensionPoint {

	/**
	 * Retrieve the path of page that will generate browser tabs icon and title based on available
	 * <b>context instance</b> and <b>URL path.</b>
	 */
	@Extension(target = BrowserTabs.EXTENSION_POINT, enabled = true, order = 10, priority = 2)
	public static class ApplicationTabHeader implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/main-browser-tabs.xhtml";
		}
	}

}
