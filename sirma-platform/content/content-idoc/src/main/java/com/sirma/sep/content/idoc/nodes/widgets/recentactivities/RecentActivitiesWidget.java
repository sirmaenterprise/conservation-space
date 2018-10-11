package com.sirma.sep.content.idoc.nodes.widgets.recentactivities;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default recent activities widget implementation. It will be used as server side recent activities widget object with
 * own configuration and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidget extends WidgetNode {

	public static final String NAME = "recent-activities";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public RecentActivitiesWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new RecentActivitiesWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new RecentActivitiesWidgetConfiguration(this, config);
	}

}
