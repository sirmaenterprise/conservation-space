package com.sirma.sep.content.idoc.nodes.widgets.chart;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.WidgetConfiguration;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default Chart widget implementation.
 *
 * @author hlungov
 */
public class ChartWidget extends WidgetNode {

	public static final String CHART_WIDGET_NAME = "chart-view-widget";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ChartWidget(Element node) {
		super(node);
	}

	@Override
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new ChartWidgetConfiguration(this, config);
	}

	@Override
	protected WidgetConfiguration buildConfig(String config) {
		return new ChartWidgetConfiguration(this, config);
	}

}
