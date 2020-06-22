package com.sirma.sep.content.idoc.nodes.widgets.image;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of image widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ImageWidgetConfiguration extends WidgetConfiguration {

	private static final String LOCK_WIDGET_KEY = "lockWidget";

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public ImageWidgetConfiguration(Widget widget, String configuration) {
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
	public ImageWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Sets property 'lockWidget' to <code>true</code> in the current configuration, which will lock the widget and
	 * prevent any edit operation for it.
	 */
	public void lockWidget() {
		addNotNullProperty(LOCK_WIDGET_KEY, new JsonPrimitive(true));
	}

	/**
	 * Sets property 'lockWidget' to <code>false</code> in the current configuration, which will unlock the widget for
	 * editing the widget in which is stored the configuration.
	 */
	public void unlockWidget() {
		addNotNullProperty(LOCK_WIDGET_KEY, new JsonPrimitive(false));
	}

}
