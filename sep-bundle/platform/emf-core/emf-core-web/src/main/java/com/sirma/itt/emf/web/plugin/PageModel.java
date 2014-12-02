package com.sirma.itt.emf.web.plugin;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * The Interface PageModel.
 *
 * @author hlungov
 */
public interface PageModel extends Plugin {

	/**
	 * Get html of extension point.
	 *
	 * @return the string
	 */
	String getPageFragment();

}
