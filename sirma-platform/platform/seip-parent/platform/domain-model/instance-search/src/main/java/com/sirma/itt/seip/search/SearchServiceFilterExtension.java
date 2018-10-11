package com.sirma.itt.seip.search;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Extension for {@link SearchService}. Realizes the concrete search implementations per object type and query language.
 *
 * @author BBonev
 */
public interface SearchServiceFilterExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "searchServiceExtension";

	/**
	 * Gets the filter by name and initialized using the given context if any.
	 *
	 * @param <S>
	 *            the result arguments type
	 * @param filterName
	 *            the filter name
	 * @param context
	 *            the context if any, could be <code>null</code>.
	 * @return the filter or <code>null</code> if not found/supported.
	 */
	<S extends SearchArguments<?>> S buildSearchArguments(String filterName, Context<String, Object> context);

	/**
	 * Builds the search arguments from the given filter object
	 *
	 * @param <S>
	 *            the generic type
	 * @param filter
	 *            the filter object to be used as provider when building the arguments.
	 * @param context
	 *            the context that is used to provide arguments to be used when building the query.
	 * @return the builded search arguments filter that could be executed by a supported search engine.
	 */
	<S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Context<String, Object> context);

	/**
	 * Gets the list of supported filters for the given placeholder.
	 *
	 * @param placeHolder
	 *            the place holder
	 * @return the filters
	 */
	SearchFilterConfig getFilterConfiguration(String placeHolder);
}
