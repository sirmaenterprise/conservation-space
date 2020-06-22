package com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link AggregatedTableWidget} that represents a basic server side implementation of the aggregated
 * table widget.
 *
 * @author A. Kunchev
 */
public class AggregatedTableWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME)
				&& AggregatedTableWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new AggregatedTableWidget(element);
	}

}
