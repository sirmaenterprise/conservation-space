package com.sirma.sep.content.idoc.extensions.widgets.image;

import java.util.Map;

import com.google.gson.JsonElement;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidgetConfiguration;

/**
 * Base version handler for {@link ImageWidget}. It will retrieved the ids of the instances that are shown in the
 * widget, convert them in to version ids and store them in the widget configuration as selected objects. Uses default
 * handle implementation from the super class to complete this process. Also makes the content displayed in the widget
 * uneditable by changing specific widget attribute and configuration.
 *
 * @author A. Kunchev
 */
public class ImageWidgetVersionHandler extends AbstractWidgetVersionHandler<ImageWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ImageWidget;
	}

	@Override
	public HandlerResult handle(ImageWidget widget, HandlerContext context) {
		ImageWidgetConfiguration configuration = widget.getConfiguration();
		Map<String, JsonElement> configMap = GSON.fromJson(configuration.getConfiguration(), CONFIGURATION_MAP_TYPE);
		HandlerResult result = super.handle(widget, context);
		configuration.lockWidget();
		super.storeConfigurationDiff(configMap, configuration);
		return result;
	}

	@Override
	protected void storeConfigurationDiff(Map<String, JsonElement> originalConfigurationMap,
			WidgetConfiguration configuration) {
		// do nothing for this widget, when called from super.handler
		// the logic that this method is responsible for, will be executed later in this class handle
	}

}
