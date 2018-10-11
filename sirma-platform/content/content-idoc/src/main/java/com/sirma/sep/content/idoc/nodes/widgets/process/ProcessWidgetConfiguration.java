package com.sirma.sep.content.idoc.nodes.widgets.process;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents the default configuration of process widget.
 *
 * @author hlungov
 */
public class ProcessWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ProcessWidgetConfiguration(Widget widget, String configuration) {
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
	public ProcessWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

}
