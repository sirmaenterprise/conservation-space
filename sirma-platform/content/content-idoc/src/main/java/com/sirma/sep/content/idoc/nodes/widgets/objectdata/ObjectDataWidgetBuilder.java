package com.sirma.sep.content.idoc.nodes.widgets.objectdata;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ObjectDataWidget} that represents a basic server side implementation of the object data
 * widget.
 *
 * @author A. Kunchev
 */
public class ObjectDataWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ObjectDataWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new ObjectDataWidget(element);
	}

}
