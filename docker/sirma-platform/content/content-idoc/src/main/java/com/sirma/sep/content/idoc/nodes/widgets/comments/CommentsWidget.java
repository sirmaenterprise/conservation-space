package com.sirma.sep.content.idoc.nodes.widgets.comments;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default comments widget implementation. It will be used as server side comments widget object with own configuration
 * and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class CommentsWidget extends WidgetNode {

	public static final String NAME = "comments-widget";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public CommentsWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new CommentsWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new CommentsWidgetConfiguration(this, config);
	}

}
