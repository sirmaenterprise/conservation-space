package com.sirma.sep.content.idoc.nodes.widgets.objectdata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of object data widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ObjectDataWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ObjectDataWidgetConfiguration(Widget widget, String configuration) {
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
	public ObjectDataWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Get the selected instance if explicit instance is selected
	 *
	 * @return the selected instance or null
	 */
	public String getSelectedInstance() {
		JsonElement selectedInstance = getConfiguration().get("selectedInstance");
		if (selectedInstance != null) {
			return selectedInstance.getAsString();
		}
		return null;
	}

	/**
	 * Remove any selected instance and changes the selectObjectMode to 'current' and the selection to single
	 */
	public void setSelectedInstanceToCurrent() {
		// remove previous selected instance
		getConfiguration().remove("selectedInstance");
		// change the selectedObjectMode to 'current'
		getConfiguration().addProperty("selectObjectMode", "current");
		getConfiguration().addProperty("selection", "single");
	}

}
