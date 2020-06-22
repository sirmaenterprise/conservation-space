package com.sirma.sep.content.idoc.nodes.widgets.insertlink;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Default object link widget implementation. It will be used as server side object link widget with own configuration
 * and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ObjectLinkWidgetConfiguration(Widget widget, String configuration) {
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
	public ObjectLinkWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

}
