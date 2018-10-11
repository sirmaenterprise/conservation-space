package com.sirma.sep.content.idoc.extensions.widgets.comments;

import static com.sirma.sep.content.idoc.extensions.widgets.comments.CommentsWidgetSearchHandler.INSTANCE_IDS_RESULT_MAP_KEY;

import java.util.Date;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetHandlersUtil;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidgetConfiguration;

/**
 * Base version handler for {@link CommentsWidget}. It extracts the results retrieved by
 * {@link CommentsWidgetSearchHandler} and stores them in the widget configuration. Additionally this handler creates
 * {@link DateRange}, with which will be created comments request for specific time period. This period is limited which
 * end date, which is the date when the version was created. This way the widget will retrieve correct data by
 * requesting comments that are created before that date. The changes made to the widget configuration are:
 * <ol>
 * <li>Change the widget selection mode to 'manually'</li>
 * <li>Add found instance ids as selected objects in the widget</li>
 * <li>Update/Add 'filterCriteria' property with the date range that limits the period for which the comments should be
 * retrieved.</li>
 * </ol>
 *
 * @author A. Kunchev
 */
public class CommentsWidgetVersionHandler extends AbstractWidgetVersionHandler<CommentsWidget> {

	private static final String CUSTOM_FILTER_CRITERIA_OPERATOR = "version";
	private static final String CREATED_ON_SHORT_URI = EMF.PREFIX + ":" + EMF.CREATED_ON.getLocalName();
	private static final String FILTER_CRITERIA_TYPE_VALUE = "dateTime";

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof CommentsWidget;
	}

	@Override
	protected HandlerResult processResults(CommentsWidget widget, WidgetResults searchResults, Date versionDate) {
		Map<String, Object> resultMap = searchResults.getResultsAsMap();
		CommentsWidgetConfiguration configuration = widget.getConfiguration();
		setDateRangeConfiguration(resultMap.get(CommentsWidgetSearchHandler.DATE_RANGE_KEY), configuration);
		configuration.setSelectionMode(WidgetSelectionMode.MANUALLY);
		configuration.setSelectedObjects(extractResults(searchResults.isFoundBySearch(),
				() -> WidgetHandlersUtil.getCollectionFromMap(INSTANCE_IDS_RESULT_MAP_KEY, resultMap)));
		return new HandlerResult(widget, GSON.toJsonTree(resultMap));
	}

	/**
	 * Builds date range based on the {@link Date} when the version is created. This way the widget will build and send
	 * request that will extract comments for limited period of time and get only the comments that are created/added
	 * before the version creation.
	 */
	private static void setDateRangeConfiguration(Object range, CommentsWidgetConfiguration configuration) {
		if (!(range instanceof DateRange)) {
			return;
		}

		DateRange dateRange = (DateRange) range;
		JsonObject filterCriteria = configuration.getFilterCriteria();
		if (filterCriteria != null && !filterCriteria.isJsonNull() && !filterCriteria.entrySet().isEmpty()) {
			buildVersionFilterCriteria(dateRange, filterCriteria);
		} else {
			filterCriteria = buildNewFilterCriteria(dateRange);
		}

		configuration.setFilterCriteria(filterCriteria);
	}

	private static void buildVersionFilterCriteria(DateRange dateRange, JsonObject filterCriteria) {
		// 'version' is custom operator that is set so the web uses the dates build below (automatically)
		filterCriteria.addProperty(JsonKeys.OPERATOR, CUSTOM_FILTER_CRITERIA_OPERATOR);
		JsonArray dates = new JsonArray();
		if (dateRange.getFirst() != null) {
			dates.add(new JsonPrimitive(ISO8601DateFormat.format(dateRange.getFirst())));
		} else {
			dates.add(new JsonPrimitive(""));
		}

		// for versions we always have second date(version creation date)
		dates.add(new JsonPrimitive(ISO8601DateFormat.format(dateRange.getSecond())));
		filterCriteria.add(JsonKeys.VALUE, dates);
	}

	private static JsonObject buildNewFilterCriteria(DateRange range) {
		JsonObject filterCriteria = new JsonObject();
		filterCriteria.addProperty(JsonKeys.FIELD, CREATED_ON_SHORT_URI);
		filterCriteria.addProperty(JsonKeys.TYPE, FILTER_CRITERIA_TYPE_VALUE);
		buildVersionFilterCriteria(range, filterCriteria);
		return filterCriteria;
	}
}
