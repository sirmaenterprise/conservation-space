package com.sirma.sep.content.idoc.nodes.widgets.content;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ContentViewerWidget} that represents a basic server side implementation of the content viewer
 * widget.
 *
 * @author A. Kunchev
 */
public class ContentViewerWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ContentViewerWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new ContentViewerWidget(element);
	}

}
