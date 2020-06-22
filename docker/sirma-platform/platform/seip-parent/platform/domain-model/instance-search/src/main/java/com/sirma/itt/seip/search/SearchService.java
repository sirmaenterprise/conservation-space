package com.sirma.itt.seip.search;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.SearchRequest;

/**
 * Single entry point for searching data in EMF. The service provides means to get predefined search filters also as
 * executing a search under various search engines.
 *
 * @author BBonev
 */
public interface SearchService {

	/**
	 * Gets a filter by the specified name for the given argument class type. If the filter requires any additional
	 * initializing it should be passed via the {@link Context} parameter.
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
	 * @return the builded search arguments filter that could be executed by a supported search engine.
	 */
	<S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Class<?> resultType,
			Context<String, Object> context);

	/**
	 * Perform a search for the given arguments without additional loading for the found results.
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

	/**
	 * Perform a search for the given arguments and loads the found results.
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
	<E extends Instance, S extends SearchArguments<E>> void searchAndLoad(Class<?> target, S arguments);

	/**
	 * Perform search operation and stream results by transforming the data using the given result transformer.<br>
	 * The main use for this method is to allow processing huge amounts of data by streaming the data from the
	 * database without loading all data.
	 * <p><b>Note that the returned stream should be closed after use. Failing to do
	 * so may lead to memory or other resource leak!</b>
	 * <br>Suggested use:
	 * <pre><code>
	 * try (Stream&lt;String&gt; stream = searchService.stream(arguments, transformer) {
	 *     // use the stream as normal
	 * }
	 *
	 * </code>
	 * </pre></p>
	 *
	 * @param <S> search arguments type
	 * @param <R> the expected result type after transformation
	 * @param arguments the search arguments to use for search execution
	 * @param resultTransformer the result transformer to apply
	 * @return stream of data after transforming
	 */
	<R, S extends SearchArguments<? extends  Instance>> Stream<R> stream(S arguments, ResultItemTransformer<R>
			resultTransformer);

	/**
	 * Perform search aggregation function using the given query arguments. The result mapping will consist of all
	 * properties chosen for aggregation and provided by {@link SearchArguments#getGroupBy()}. The keys of the second
	 * map are the aggregated values and their counts.
	 * <br>Note that the {@link SearchArguments#getGroupBy()} should be filled before calling this method.
	 *
	 * @param <S> search arguments type
	 * @param arguments the search arguments to use for search execution
	 * @return a mapping with all aggregated data
	 */
	<S extends SearchArguments<? extends Instance>> Map<String, Map<String, Integer>> groupBy(S arguments);

	/**
	 * Parses the given search request into proper search arguments.
	 *
	 * @param <E>
	 *            the Instance type
	 * @param <S>
	 *            the arguments type
	 * @param request
	 *            the request to parse
	 * @return the build arguments or <code>null</code> if the request was not possible to produce any valid search
	 *         request.
	 */
	<E extends Instance, S extends SearchArguments<E>> S parseRequest(SearchRequest request);

	/**
	 * Get escape function that can be used to escape text data for the given dialect.
	 *
	 * @param dialect
	 *            the dialect
	 * @return the escape function.
	 */
	Function<String, String> escapeForDialect(String dialect);
}
