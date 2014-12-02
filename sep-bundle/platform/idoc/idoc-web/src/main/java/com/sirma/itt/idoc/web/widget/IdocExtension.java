package com.sirma.itt.idoc.web.widget;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * Idoc extensions/plugins/widgets.
 * 
 * @author svelikov
 */
public interface IdocExtension extends Plugin {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "idoc.widget";

	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	String getPath();

	/**
	 * Checks for stylesheet.
	 * 
	 * @return the boolean
	 */
	Boolean hasStylesheet();
}
