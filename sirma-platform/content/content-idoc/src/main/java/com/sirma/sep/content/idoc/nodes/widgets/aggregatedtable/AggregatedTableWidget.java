package com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default aggregated table widget implementation. It will be used as server side aggregated table widget object with
 * own configuration and logic. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class AggregatedTableWidget extends WidgetNode {

	public static final String NAME = "aggregated-table";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public AggregatedTableWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new AggregatedTableConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new AggregatedTableConfiguration(this, config);
	}

}
