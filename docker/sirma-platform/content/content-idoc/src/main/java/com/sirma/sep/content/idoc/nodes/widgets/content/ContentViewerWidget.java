package com.sirma.sep.content.idoc.nodes.widgets.content;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default content viewer widget implementation. It will be used as server side content viewer widget object with own
 * configuration and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ContentViewerWidget extends WidgetNode {

	public static final String NAME = "content-viewer";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ContentViewerWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new ContentViewerWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new ContentViewerWidgetConfiguration(this, config);
	}

}
