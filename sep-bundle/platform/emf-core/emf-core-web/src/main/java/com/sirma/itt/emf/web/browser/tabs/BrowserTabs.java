package com.sirma.itt.emf.web.browser.tabs;

import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * This class define plugin identifier for create icons and titles for browser tabs.
 * 
 * @author cdimitrov
 */
public interface BrowserTabs extends Plugable {

	/** Extension point identifier constant. */
	public static final String EXTENSION_POINT = "browser-tabs";

}
