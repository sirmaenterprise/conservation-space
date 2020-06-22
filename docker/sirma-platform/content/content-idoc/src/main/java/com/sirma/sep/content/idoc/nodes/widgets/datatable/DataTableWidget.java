package com.sirma.sep.content.idoc.nodes.widgets.datatable;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default data table widget implementation. It will be used as server side data table widget object with own
 * configuration and logic. Could be extended further, if needed.
 *
 * @author BBonev
 */
public class DataTableWidget extends WidgetNode {

	public static final String NAME = "datatable-widget";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public DataTableWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new DataTableWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new DataTableWidgetConfiguration(this, config);
	}
}
