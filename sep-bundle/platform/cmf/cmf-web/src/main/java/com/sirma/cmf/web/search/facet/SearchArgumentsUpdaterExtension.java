package com.sirma.cmf.web.search.facet;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.plugin.SupportablePlugin;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The Interface SearchArgumentsUpdaterExtension.
 * 
 * @param <E>
 *            the element type
 */
public interface SearchArgumentsUpdaterExtension<E extends Entity> extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "search.arguments.updater.extension";

	/**
	 * Update arguments where necessary.
	 * 
	 * @param searchArguments
	 *            the search arguments
	 */
	void updateArguments(SearchArguments<E> searchArguments);
}
