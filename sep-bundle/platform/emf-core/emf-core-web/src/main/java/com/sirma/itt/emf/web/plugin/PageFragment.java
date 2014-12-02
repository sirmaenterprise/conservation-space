package com.sirma.itt.emf.web.plugin;

import com.sirma.itt.emf.plugin.PathDefinition;

/**
 * Represented a page fragment that should pluged in a web page.
 * 
 * @author Adrian Mitev
 */
public interface PageFragment extends PathDefinition {

	/**
	 * Path to the page fragment file.
	 * 
	 * @return path within the servlet context or class path.
	 */
	@Override
	String getPath();
}
