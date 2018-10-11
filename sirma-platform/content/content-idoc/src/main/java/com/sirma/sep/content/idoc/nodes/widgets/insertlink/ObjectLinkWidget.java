package com.sirma.sep.content.idoc.nodes.widgets.insertlink;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default object link widget implementation. It will be used as server side data table widget object with own
 * configuration and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWidget extends WidgetNode {

	public static final String NAME = "object-link";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ObjectLinkWidget(Element node) {
		super(node);
	}

	@Override
	protected ObjectLinkWidgetConfiguration buildConfig(JsonObject config) {
		return new ObjectLinkWidgetConfiguration(this, config);
	}

	@Override
	protected ObjectLinkWidgetConfiguration buildConfig(String config) {
		return new ObjectLinkWidgetConfiguration(this, config);
	}

}
