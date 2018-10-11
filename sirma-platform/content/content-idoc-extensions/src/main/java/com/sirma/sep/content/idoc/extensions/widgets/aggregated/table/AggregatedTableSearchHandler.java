package com.sirma.sep.content.idoc.extensions.widgets.aggregated.table;

import static com.sirma.itt.seip.search.SearchQueryParameters.GROUP_BY;
import static com.sirma.itt.seip.search.SearchQueryParameters.SELECTED_OBJECTS;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.AbstractAggregatedSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableConfiguration;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

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
public class AggregatedTableSearchHandler extends AbstractAggregatedSearchHandler<AggregatedTableWidget> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String NAME_KEY = "name";

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof AggregatedTableWidget;
	}

	@Override
	protected SearchRequest buildSearchRequest(AggregatedTableWidget widget, HandlerResult searchResult) {
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
		Optional<javax.json.JsonObject> searchCriteria = configuration.getSearchCriteria();
		if (searchCriteria.isPresent()) {
			Condition condition = jsonToConditionConverter.parseCondition(searchCriteria.get());
            ConditionBuilder builder = SearchCriteriaBuilder.createConditionBuilder().from(condition);
            jsonToDateRangeConverter.populateConditionRuleWithDateRange(builder, condition);
            request.setSearchTree(builder.build());
		}
		return request;
	}
}
