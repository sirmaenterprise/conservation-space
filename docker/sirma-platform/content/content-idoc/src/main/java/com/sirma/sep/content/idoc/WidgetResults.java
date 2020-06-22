package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;

/**
 * Represents temporary store for the widget results that should be shown in it. This object will not be persisted
 * anywhere, it is used just to store data retrieved by {@link SearchContentNodeHandler} so that we could use this
 * results for further processing, like versioning, etc.
 *
 * @author A. Kunchev
 */
public class WidgetResults {

	/**
	 * Does not contain results and {@link #isFoundBySearch()} will always return {@code false}.
	 */
	public static final WidgetResults EMPTY = new WidgetResults(null, false);

	/**
	 * Store for the results that are shown in the widget.
	 */
	private Optional<Object> results = Optional.empty();

	/**
	 * Used to show whether the results are found by actual search. Should be set to {@code false} if the results are
	 * directly retrieved from the configuration(mainly when the objects are selected manually).
	 */
	private final boolean foundBySearch;

	/**
	 * Instantiates new widget results object.
	 *
	 * @param results the results that should be stored
	 * @param foundBySearch if the results are actually retrieved by search or not
	 */
	private WidgetResults(final Object results, final boolean foundBySearch) {
		this.results = Optional.ofNullable(results);
		this.foundBySearch = foundBySearch;
	}

	/**
	 * Creates new {@link WidgetResults} object, where {@link #isFoundBySearch()} will always return {@code false}.
	 *
	 * @param results that are shown in the widget
	 * @return new {@link WidgetResults} object
	 */
	public static WidgetResults fromConfiguration(final Object results) {
		return new WidgetResults(results, false);
	}

	/**
	 * Creates new {@link WidgetResults} object, where {@link #isFoundBySearch()} will always return {@code true}.
	 *
	 * @param results that are shown in the widget
	 * @return new {@link WidgetResults} object
	 */
	public static WidgetResults fromSearch(final Object results) {
		return new WidgetResults(results, true);
	}

	/**
	 * Shows if this object contains any results.
	 *
	 * @return {@code true} if the current object contains results, {@code false} otherwise
	 */
	public boolean areAny() {
		return !EMPTY.equals(this) && results.isPresent();
	}

	/**
	 * Retrieves stored results as {@link Map}. If there are no results or the results are equals to {@link #EMPTY}, the
	 * method will return empty {@link Map}.
	 *
	 * @return stored results as {@link Map} or empty {@link Map} if there are no resultss
	 */
	public Map<String, Object> getResultsAsMap() {
		return results.map(Map.class::cast).orElse(emptyMap());
	}

	/**
	 * Retrieves stored results as {@link Collection}. If there are no results or the results are equals to
	 * {@link #EMPTY}, the method will return empty {@link Collection}.
	 *
	 * @param <S> the type of the result objects
	 * @return stored results as {@link Collection} or empty {@link Collection} if there are no results
	 */
	public <S extends Serializable> Collection<S> getResultsAsCollection() {
		return results.map(Collection.class::cast).orElse(emptyList());
	}

	/**
	 * Shows if the current results are found by actual search operation or not.
	 *
	 * @return {@code true} if the current results are found through search, {@code false} otherwise
	 */
	public boolean isFoundBySearch() {
		return foundBySearch;
	}
}
