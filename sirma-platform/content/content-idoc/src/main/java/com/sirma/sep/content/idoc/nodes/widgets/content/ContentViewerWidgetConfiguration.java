package com.sirma.sep.content.idoc.nodes.widgets.content;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of content viewer widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ContentViewerWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ContentViewerWidgetConfiguration(Widget widget, String configuration) {
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
	public ContentViewerWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

}
