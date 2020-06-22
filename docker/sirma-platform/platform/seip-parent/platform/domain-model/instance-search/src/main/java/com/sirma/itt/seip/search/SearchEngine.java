package com.sirma.itt.seip.search;

import java.util.function.Function;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension for {@link com.sirma.itt.seip.search.SearchService} that realize a concrete search algorithms.
 *
 * @author BBonev
 */
public interface SearchEngine extends Plugin {

	/** The target name. */
	String TARGET_NAME = "searchEngine";

	/**
	 * Checks if is supported.
	 *
	 * @param <S>
	 *            the generic type
	 * @param target
	 *            the target
	 * @param arguments
	 *            the arguments
	 * @return true, if is supported
	 */
	<S extends SearchArguments<? extends Instance>> boolean isSupported(Class<?> target, S arguments);

	/**
	 * Perform the search for the given target and the given arguments.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param target
	 *            the target
	 * @param arguments
	 *            the arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void search(Class<?> target, S arguments);

	/**
	 * Execute the given query and stream he raw results
	 *
	 * @param arguments the search request to invoke
	 * @param <S> the request type
	 * @return stream of result items
	 */
	<S extends SearchArguments<? extends Instance>> Stream<ResultItem> stream(S arguments);

	/**
	 * Checks if is applicable for the arguments - can handle such arguments and if not return false. Otherwise prepare
	 * basic query based on the arguments map and update the provided {@link SearchArguments}
	 *
	 * @param request
	 *            the query arguments to handle - with the specified key values for basic search
	 * @param searchArguments
	 *            the arguments to update
	 * @return true if {@link SearchArguments} are updated and false if this engine is not applicable
	 */
	boolean prepareSearchArguments(SearchRequest request, SearchArguments<Instance> searchArguments);

	/**
	 * Get escape function that can be used to escape text data for the given dialect.
	 *
	 * @param dialect
	 *            the dialect
	 * @return the escape function.
	 */
	Function<String, String> escapeForDialect(String dialect);
}
