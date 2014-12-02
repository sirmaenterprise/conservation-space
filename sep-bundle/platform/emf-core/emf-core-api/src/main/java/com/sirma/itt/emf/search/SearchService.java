package com.sirma.itt.emf.search;

import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;

/**
 * Single entry point for searching data in EMF. The service provides means to get predefined search
 * filters also as executing a search under various search engines.
 *
 * @author BBonev
 */
public interface SearchService {

	/**
	 * Gets a filter by the specified name for the given argument class type. If the filter requires
	 * any additional initializing it should be passed via the {@link Context} parameter.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param filterName
	 *            the filter name to get
	 * @param resultType
	 *            the main result type
	 * @param context
	 *            the context used to pass arguments, could be <code>null</code>.
	 * @return the build filter filter or <code>null</code> if no such filter exists.
	 */
	<E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context);

	/**
	 * Gets the filters identifiers for the given placeholder and result type.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param placeHolder
	 *            the target place holder
	 * @param resultType
	 *            he expected main result type
	 * @return the filters list for the given place holder
	 */
	<E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType);

	/**
	 * Builds the search arguments from the given filter object.
	 * 
	 * @param <S>
	 *            the search arguments type
	 * @param filter
	 *            the filter object to be used as provider when building the arguments.
	 * @param resultType
	 *            the result type
	 * @param context
	 *            the context that is used to provide arguments to be used when building the query.
	 * @return the builded search arguments filter that could be executed by a supported search
	 *         engine.
	 */
	<S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
			Context<String, Object> context);

	/**
	 * Perform a search for the given arguments.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param target
	 *            the main expected result type
	 * @param arguments
	 *            the search arguments to execute
	 */
	<E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments);
}
