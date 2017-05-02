package com.sirmaenterprise.sep.content.idoc.extensions.widgets.aggregatedtable;

import static com.sirmaenterprise.sep.content.idoc.extensions.widgets.aggregatedtable.AggregatedTableSearchHandler.INSTANCE_IDS_RESULT_MAP_KEY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.itt.seip.instance.version.VersionProperties;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Base version handler for {@link AggregatedTableWidget}. This handler uses the results retrieved by the
 * {@link AggregatedTableSearchHandler}. It will extract the id of the instances displayed in the widget, convert them
 * in version ids and store them back in the result map. The results from the handlers processing are stored in the
 * widget configuration as new property under specific key. This property is then used to load the version data for the
 * widget, when specific instance version is loaded/opened. <br />
 * If there are no results from the {@link AggregatedTableSearchHandler}, by default the handler will store empty object
 * for the version property.
 * <p>
 * <b>NOTE - the additional version property for this widget should not be used for any other reason, but storing
 * version data!</b>
 *
 * @author A. Kunchev
 */
public class AggregatedTableVersionHandler extends AbstractWidgetVersionHandler<AggregatedTableWidget> {

	public static final String VERSION_DATA_CONFIG_KEY = "versionData";

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof AggregatedTableWidget;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HandlerResult handle(AggregatedTableWidget widget, HandlerContext context) {
		Optional<Object> searchResults = widget.getConfiguration().getSearchResults();
		Date versionDate = context.getIfSameType(VersionProperties.HANDLERS_CONTEXT_VERSION_DATE_KEY, Date.class);
		if (!searchResults.isPresent() || versionDate == null) {
			// store empty object
			widget.getConfiguration().addNotNullProperty(VERSION_DATA_CONFIG_KEY, new JsonObject());
			return new HandlerResult(widget);
		}

		Map<String, Object> resultMap = (Map<String, Object>) searchResults.get();
		Collection<Serializable> ids = getCollectionFromMap(INSTANCE_IDS_RESULT_MAP_KEY, resultMap);
		Map<Serializable, Serializable> versionIdsMap = versionDao.findVersionIdsByTargetIdAndDate(ids, versionDate);
		// replace the ids with the version ids
		resultMap.replace(INSTANCE_IDS_RESULT_MAP_KEY, versionIdsMap.values());
		JsonElement resultJson = GSON.toJsonTree(resultMap);
		widget.getConfiguration().addNotNullProperty(VERSION_DATA_CONFIG_KEY, resultJson);
		return new HandlerResult(widget, resultJson);
	}

}
