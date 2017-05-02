package com.sirmaenterprise.sep.content.idoc.extensions.widgets.aggregatedtable;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.search.SearchQueryParameters.GROUP_BY;
import static com.sirma.itt.seip.search.SearchQueryParameters.SELECTED_OBJECTS;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableConfiguration;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Base search handler for {@link AggregatedTableWidget}. This handler will execute two searches, one for the instances
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
 * @author A. Kunchev
 */
public class AggregatedTableSearchHandler extends AbstractWidgetSearchHandler<AggregatedTableWidget> {

	static final String INSTANCE_IDS_RESULT_MAP_KEY = "instanceIds";
	static final String AGGREGATED_DATA_RESULT_MAP_KEY = "aggregatedData";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String NAME_KEY = "name";

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof AggregatedTableWidget;
	}

	@Override
	public HandlerResult handle(AggregatedTableWidget node, HandlerContext context) {
		WidgetConfiguration configuration = node.getConfiguration();
		HandlerResult searchResult = super.handle(node, context);
		// if there are no results from the first search then there is no need to try and search for aggregated data.
		if (getResults(searchResult).isEmpty()) {
			configuration.setSearchResults(null);
			return new HandlerResult(node);
		}
		SearchRequest request = buildSearchRequest(node, searchResult);
		SearchArguments<Instance> arguments = search(request, context.getCurrentInstanceId());
		Map<String, Object> resultMap = new HashMap<>(2);
		resultMap.put(AGGREGATED_DATA_RESULT_MAP_KEY, arguments.getAggregatedData());
		resultMap.put(INSTANCE_IDS_RESULT_MAP_KEY, request.get(SELECTED_OBJECTS));
		configuration.setSearchResults(resultMap);
		return new HandlerResult(node, resultMap);
	}

	/**
	 * Builds search request with {@link SearchQueryParameters#GROUP_BY} clause, if there is such property passed in the
	 * widget configuration. This request is used for the second search that aggregates the results for the main search
	 * that only finds the instances that should be used for the aggregation.
	 */
	private SearchRequest buildSearchRequest(AggregatedTableWidget widget, HandlerResult searchResult) {
		Map<String, List<String>> requestMap = new HashMap<>(2);
		AggregatedTableConfiguration configuration = widget.getConfiguration();
		JsonObject groupBy = configuration.getGroupBy();
		if (groupBy != null && StringUtils.isNoneBlank(groupBy.get(NAME_KEY).getAsString())) {
			requestMap.put(GROUP_BY, Arrays.asList(groupBy.get(NAME_KEY).getAsString()));
		} else {
			LOGGER.warn("There is no [{}] property for widget with id - {}", GROUP_BY, widget.getId());
		}

		requestMap.put(SELECTED_OBJECTS, getResults(searchResult));
		SearchRequest request = new SearchRequest(requestMap);
		Condition condition = jsonToConditionConverter.parseCondition(configuration.getSearchCriteria().get());
		request.setSearchTree(condition);
		return request;
	}

	/**
	 * Retrieves the main search results as {@link List}. This results are the ids of the found instances from the main
	 * search.
	 */
	private static List<String> getResults(HandlerResult searchResult) {
		Optional<Collection<String>> result = searchResult.getResult();
		if (result.isPresent()) {
			return new ArrayList<>(result.get());
		}

		return emptyList();
	}

}
