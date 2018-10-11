package com.sirma.sep.content.idoc.extensions.widgets.aggregated;

import static com.sirma.itt.seip.search.SearchQueryParameters.SELECTED_OBJECTS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;

/**
 * Base search handler for Widgets with aggregated data. This handler will execute two searches, one for the instances
 * that should be aggregated and the second one for the actual aggregating of the results from the first search.<br />
 * As result this handler will return {@link Map}, containing the aggregated data and the instance ids for which the
 * aggregation is done.
 * <p>
 * Result map data:
 * <table border="1">
 * <tr align="center">
 * <td>key</td>
 * <td>value</td>
 * </tr>
 * <tr align="center">
 * <td>aggregatedData</td>
 * <td>the data returned from the aggregated search. The data is represented by {@link Map}. For more information see
 * {@link SearchArguments#getAggregatedData()}</td>
 * </tr>
 * <tr align="center">
 * <td>instanceIds</td>
 * <td>{@link List} of ids of the instances for which the aggregation is done. Could be used for other kind of
 * processing, if needed (versioning for example)</td>
 * </tr>
 * </table>
 *
 * @param <W> the type of the widget
 * @author hlungov
 */
public abstract class AbstractAggregatedSearchHandler<W extends Widget> extends AbstractWidgetSearchHandler<W> {

	protected static final String INSTANCE_IDS_RESULT_MAP_KEY = "instanceIds";
	protected static final String AGGREGATED_DATA_RESULT_MAP_KEY = "aggregatedData";

	@Override
	public HandlerResult handle(W node, HandlerContext context) {
		WidgetConfiguration widgetConfiguration = node.getConfiguration();
		HandlerResult searchResult = super.handle(node, context);
		// if there are no results from the first search then there is no need to try and search for aggregated data.
		if (getResults(searchResult).isEmpty()) {
			widgetConfiguration.setSearchResults(WidgetResults.EMPTY);
			return new HandlerResult(node);
		}
		SearchRequest searchRequest = buildSearchRequest(node, searchResult);
		SearchArguments<Instance> searchArguments = search(searchRequest, context.getCurrentInstanceId());
		Map<String, Object> resultMap = new HashMap<>(2);
		resultMap.put(AGGREGATED_DATA_RESULT_MAP_KEY, searchArguments.getAggregatedData());
		resultMap.put(INSTANCE_IDS_RESULT_MAP_KEY, searchRequest.get(SELECTED_OBJECTS));
		widgetConfiguration.setSearchResults(WidgetResults.fromSearch(resultMap));
		return new HandlerResult(node, resultMap);
	}

	/**
	 * Builds search request with {@link SearchQueryParameters#GROUP_BY} clause, if there is such property passed in the
	 * widget configuration. This request is used for the second search that aggregates the results for the main search
	 * that only finds the instances that should be used for the aggregation.
	 *
	 * @param widget the widget for which we build search request
	 * @param searchResult the results that are found by the first(main) search. Those results should be aggregated with
	 *        second search request
	 * @return search request that should be used to execute search operation for aggregation
	 */
	protected abstract SearchRequest buildSearchRequest(W widget, HandlerResult searchResult);
}
