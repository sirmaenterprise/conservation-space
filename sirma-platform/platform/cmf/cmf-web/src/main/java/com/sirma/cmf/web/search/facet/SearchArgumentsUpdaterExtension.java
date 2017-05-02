package com.sirma.cmf.web.search.facet;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * The Interface SearchArgumentsUpdaterExtension.
 *
 * @param <E>
 *            the element type
 */
@SuppressWarnings("rawtypes")
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
