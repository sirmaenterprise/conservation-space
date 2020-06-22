package com.sirma.sep.content.idoc.nodes.widgets.objectdata;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default object data widget implementation. It will be used as server side object data widget with its own
 * configuration and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ObjectDataWidget extends WidgetNode {

	public static final String NAME = "object-data-widget";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ObjectDataWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new ObjectDataWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new ObjectDataWidgetConfiguration(this, config);
	}

}
