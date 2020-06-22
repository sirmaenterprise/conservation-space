package com.sirma.sep.content.idoc.nodes.widgets.chart;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ChartWidget} that represents a basic server side implementation of the chart widget.
 *
 * @author hlungov
 */
public class ChartWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ChartWidget.CHART_WIDGET_NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new ChartWidget(element);
	}

}
