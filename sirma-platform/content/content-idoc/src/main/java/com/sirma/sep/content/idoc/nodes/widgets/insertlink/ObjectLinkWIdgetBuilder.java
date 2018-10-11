package com.sirma.sep.content.idoc.nodes.widgets.insertlink;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ObjectLinkWidget} that represents a basic server side implementation of the object link
 * widget.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWIdgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ObjectLinkWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element node) {
		return new ObjectLinkWidget(node);
	}

}
