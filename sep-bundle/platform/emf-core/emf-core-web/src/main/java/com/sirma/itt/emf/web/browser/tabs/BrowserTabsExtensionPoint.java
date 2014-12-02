package com.sirma.itt.emf.web.browser.tabs;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * This class will holds extension for browser tab. Based on modules that are loaded will switch
 * functionality for generating tab title and icon.
 * 
 * @author cdimitrov
 */
@Named
public class BrowserTabsExtensionPoint implements BrowserTabs {

	/**
	 * Holds functionality for generating default title and icon for browser tabs.
	 */
	@Extension(target = BrowserTabs.EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class ApplicationTabHeader implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/browser-tabs.xhtml";
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
