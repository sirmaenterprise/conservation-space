package com.sirma.sep.content.idoc.nodes.widgets.chart;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of chart widget. Could be extended further, if needed.
 *
 * @author hlungov
 */
public class ChartWidgetConfiguration extends WidgetConfiguration {

	protected static final String GROUP_BY_KEY = "groupBy";
	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ChartWidgetConfiguration(Widget widget, String configuration) {
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
	public ChartWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Retrieves <b>groupBy</b> object, which holds the property used for aggregation. The actual property could be
	 * retrieved from this object by key <b>name</b>.
	 *
	 * @return {@link JsonObject} representing groupBy configuration for the widget
	 */
	public JsonElement getGroupBy() {
		return getConfiguration().has(GROUP_BY_KEY) ? getConfiguration().get(GROUP_BY_KEY) : JsonNull.INSTANCE;
	}

}
