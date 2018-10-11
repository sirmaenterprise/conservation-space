package com.sirma.sep.content.idoc.extensions.widgets.aggregated;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetHandlersUtil;

/**
 * Base version handler for Widgets with aggregated data. This handler uses the results retrieved by the current
 * implementation of {@link AbstractAggregatedSearchHandler}. It will extract the id of the instances displayed in the
 * widget, convert them in version ids and store them back in the result map. The results from the handlers processing
 * are stored in the widget configuration as new property under specific key. This property is then used to load the
 * version data for the widget, when specific instance version is loaded/opened. <br />
 * If there are no results from the search handler, by default the handler will store empty object for the version
 * property.
 *
 * @param <W> the type of the widget
 * @author hlungov
 */
public abstract class AbstractAggregatedVersionHandler<W extends Widget> extends AbstractWidgetVersionHandler<W> {

	protected static final String VERSION_DATA_CONFIG_KEY = "versionData";

	@Override
	public HandlerResult handle(W widget, HandlerContext context) {
		WidgetResults searchResults = widget.getConfiguration().getSearchResults();
		Date versionDate = context.getIfSameType(VersionProperties.HANDLERS_CONTEXT_VERSION_DATE_KEY, Date.class);
		if (!searchResults.areAny() || versionDate == null) {
			// store empty object
			widget.getConfiguration().addNotNullProperty(VERSION_DATA_CONFIG_KEY, new JsonObject());
			return new HandlerResult(widget);
		}

		Map<String, Object> resultMap = searchResults.getResultsAsMap();
		Collection<Serializable> ids = extractResults(searchResults.isFoundBySearch(), () -> WidgetHandlersUtil
				.getCollectionFromMap(AbstractAggregatedSearchHandler.INSTANCE_IDS_RESULT_MAP_KEY, resultMap));
		Map<Serializable, Serializable> versionIdsMap = versionDao.findVersionIdsByTargetIdAndDate(ids, versionDate);
		// replace the ids with the version ids
		resultMap.replace(AbstractAggregatedSearchHandler.INSTANCE_IDS_RESULT_MAP_KEY, versionIdsMap.values());
		JsonElement resultJson = GSON.toJsonTree(resultMap);
		widget.getConfiguration().addNotNullProperty(VERSION_DATA_CONFIG_KEY, resultJson);
		return new HandlerResult(widget, resultJson);
	}
}
