package com.sirmaenterprise.sep.content.idoc.extensions.widgets.recentactivities;

import static com.sirmaenterprise.sep.content.idoc.extensions.widgets.recentactivities.RecentActivitiesWidgetSearchHandler.INSTANCE_IDS_KEY;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.WidgetSelectionMode;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetConfiguration;

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
	@SuppressWarnings("unchecked")
	protected HandlerResult processResults(RecentActivitiesWidget widget, Optional<Object> searchResults,
			Date versionDate) {
		Map<String, Object> resultMap = (Map<String, Object>) searchResults.get();
		Collection<Serializable> ids = getCollectionFromMap(INSTANCE_IDS_KEY, resultMap);
		RecentActivitiesWidgetConfiguration configuration = widget.getConfiguration();
		configuration.setSelectionMode(WidgetSelectionMode.MANUALLY);
		configuration.setSelectedObjects(ids);
		return new HandlerResult(widget, resultMap);
	}

}
