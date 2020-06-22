package com.sirma.sep.content.idoc.extensions.widgets.recentactivities;

import static com.sirma.sep.content.idoc.extensions.widgets.recentactivities.RecentActivitiesWidgetSearchHandler.INSTANCE_IDS_KEY;

import java.util.Map;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.WidgetSelectionMode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.extensions.widgets.utils.WidgetHandlersUtil;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetConfiguration;

/**
 * Base version handler for {@link RecentActivitiesWidget}. This handler works with activities retrieved by the
 * {@link RecentActivitiesWidgetSearchHandler}. Only the instances ids that are returned from the search handlers are
 * used here. They are stored as selected items for the widgets and it is set to manually selected mode, so that it
 * request information about the this ids instead of performing another search.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetVersionHandler extends AbstractWidgetVersionHandler<RecentActivitiesWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof RecentActivitiesWidget;
	}

	@Override
	protected HandlerResult processResults(RecentActivitiesWidget widget, WidgetResults searchResults,
			HandlerContext context) {
		Map<String, Object> resultMap = searchResults.getResultsAsMap();
		RecentActivitiesWidgetConfiguration configuration = widget.getConfiguration();
		configuration.setSelectionMode(WidgetSelectionMode.MANUALLY);
		configuration.setSelectedObjects(extractResults(searchResults.isFoundBySearch(),
				() -> WidgetHandlersUtil.getCollectionFromMap(INSTANCE_IDS_KEY, resultMap)));
		return new HandlerResult(widget, resultMap);
	}
}