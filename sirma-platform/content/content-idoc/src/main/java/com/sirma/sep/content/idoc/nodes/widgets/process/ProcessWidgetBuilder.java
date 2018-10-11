package com.sirma.sep.content.idoc.nodes.widgets.process;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ProcessWidget} that represents a basic server side implementation of the process widget.
 *
 * @author hlungov
 */
public class ProcessWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ProcessWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new ProcessWidget(element);
	}

}