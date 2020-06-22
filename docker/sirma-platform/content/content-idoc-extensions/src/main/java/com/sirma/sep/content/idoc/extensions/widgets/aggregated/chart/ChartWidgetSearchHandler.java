package com.sirma.sep.content.idoc.extensions.widgets.aggregated.chart;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.search.SearchQueryParameters.GROUP_BY;
import static com.sirma.itt.seip.search.SearchQueryParameters.SELECTED_OBJECTS;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.AbstractAggregatedSearchHandler;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidget;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidgetConfiguration;

/**
 * Base search handler for {@link ChartWidget}.
 *
 * @author hlungov
 */
public class ChartWidgetSearchHandler extends AbstractAggregatedSearchHandler<ChartWidget> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ChartWidget;
	}

	/**
	 * Builds search request with {@link SearchQueryParameters#GROUP_BY} clause, if there is such property passed in the
	 * widget configuration. This request is used for the second search that aggregates the results for the main search
	 * that only finds the instances that should be used for the aggregation.
	 */
	@Override
	protected SearchRequest buildSearchRequest(ChartWidget widget, ContentNodeHandler.HandlerResult searchResult) {
		Map<String, List<String>> requestMap = createHashMap(2);
		ChartWidgetConfiguration configuration = widget.getConfiguration();
		JsonElement groupBy = configuration.getGroupBy();
		if (!groupBy.isJsonNull() && StringUtils.isNoneBlank(groupBy.getAsString())) {
			requestMap.put(GROUP_BY, Collections.singletonList(groupBy.getAsString()));
		} else {
			LOGGER.warn("There is no [{}] property for widget with id - {}", GROUP_BY, widget.getId());
		}
		requestMap.put(SELECTED_OBJECTS, getResults(searchResult));
		SearchRequest request = new SearchRequest(requestMap);
		configuration.getSearchCriteria()
				.map(jsonToConditionConverter::parseCondition)
				.ifPresent(request::setSearchTree);
		return request;
	}
}
