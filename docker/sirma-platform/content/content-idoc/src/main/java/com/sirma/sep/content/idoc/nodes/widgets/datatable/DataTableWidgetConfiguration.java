package com.sirma.sep.content.idoc.nodes.widgets.datatable;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of data table widget. Could be extended further, if needed.
 *
 * @author BBonev
 */
public class DataTableWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public DataTableWidgetConfiguration(Widget widget, String configuration) {
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
	public DataTableWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

}
