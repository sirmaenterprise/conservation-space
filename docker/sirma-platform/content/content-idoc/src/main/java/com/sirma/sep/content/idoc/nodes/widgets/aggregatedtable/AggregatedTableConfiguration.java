package com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of aggregated table widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class AggregatedTableConfiguration extends WidgetConfiguration {

	private static final String GROUP_BY_KEY = "groupBy";

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public AggregatedTableConfiguration(Widget widget, String configuration) {
		super(widget, configuration);
	}

	/**
	 * Constructor for widget configuration passed {@link JsonObject}.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as json
	 */
	public AggregatedTableConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Retrieves <b>groupBy</b> object, which holds the property used for aggregation. The actual property could be
	 * retrieved from this object by key <b>name</b>.
	 *
	 * @return {@link JsonObject} representing groupBy configuration for the widget
	 */
	public JsonObject getGroupBy() {
		return getConfiguration().getAsJsonObject(GROUP_BY_KEY);
	}

}
