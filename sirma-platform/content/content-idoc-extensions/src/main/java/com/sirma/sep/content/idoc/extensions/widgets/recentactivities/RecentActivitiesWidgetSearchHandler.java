package com.sirma.sep.content.idoc.extensions.widgets.recentactivities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetResults;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidget;
import com.sirma.sep.content.idoc.nodes.widgets.recentactivities.RecentActivitiesWidgetConfiguration;

/**
 * Base search handler for {@link RecentActivitiesWidget}. It handles instance search and activities search. The first
 * search is executed with the parsed criteria stored in the widget configuration. For the activities search is used
 * {@link RecentActivitiesRetriever}. <br />
 * As result this handler will return {@link StoredAuditActivitiesWrapper} object containing the information about all
 * of the found activities returned from the retriever.
 * <p>
 * Note that activities should be transformed (serialized), if you want to use them directly in the client. It is done
 * this way, because in some cases we need to apply another logic to the activities, before transforming them in format
 * suitable for displaying in the widget.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetSearchHandler extends AbstractWidgetSearchHandler<RecentActivitiesWidget> {

	static final String STORED_ACTIVITIES_KEY = "storedActivities";
	static final String INSTANCE_IDS_KEY = "instanceIds";

	private static final String INCLUDE_CURRENT_INSTANCE_KEY = "includeCurrent";

	private static final int DEFAULT_OFFSET = 0;
	private static final int DEFAULT_LIMIT_VALUE = 25;

	@Inject
	private RecentActivitiesRetriever retriever;

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof RecentActivitiesWidget;
	}

	@Override
	public HandlerResult handle(RecentActivitiesWidget node, HandlerContext context) {
		HandlerResult mainSearchResults = super.handle(node, context);
		Optional<Set<String>> searchResult = mainSearchResults.getResult();
		RecentActivitiesWidgetConfiguration configuration = node.getConfiguration();
		if (!searchResult.isPresent()) {
			// we make sure to clean up the store if no results are found from this search
			configuration.setSearchResults(WidgetResults.EMPTY);
			return new HandlerResult(node);
		}

		Set<String> ids = searchResult.get();
		Boolean includeCurrent = configuration.getProperty(INCLUDE_CURRENT_INSTANCE_KEY, Boolean.class);
		if (includeCurrent != null && includeCurrent.booleanValue()) {
			ids.add(context.getCurrentInstanceId());
		}

		StoredAuditActivitiesWrapper activities = retriever.getActivities(new ArrayList<>(ids), DEFAULT_OFFSET,
				configuration.getLimit(DEFAULT_LIMIT_VALUE));
		Map<String, Object> resultMap = new HashMap<>(2);
		resultMap.put(STORED_ACTIVITIES_KEY, activities);
		resultMap.put(INSTANCE_IDS_KEY, ids);
		configuration.setSearchResults(WidgetResults.fromSearch(resultMap));
		return new HandlerResult(node, resultMap);
	}
}
