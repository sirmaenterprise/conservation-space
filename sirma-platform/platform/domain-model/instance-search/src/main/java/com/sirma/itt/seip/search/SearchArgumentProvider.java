package com.sirma.itt.seip.search;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides pluggable search arguments.
 *
 * @author sdjulgerova
 */
public interface SearchArgumentProvider extends Plugin {

	/** Extension point name */
	String ARGUMENTS_EXTENSION_POINT = "searchArguments";

	/**
	 * Provides specific arguments for search context.
	 *
	 * @param request
	 *            - search request
	 * @param context
	 *            - search context
	 */
	void provide(SearchRequest request, Context<String, Object> context);
}
