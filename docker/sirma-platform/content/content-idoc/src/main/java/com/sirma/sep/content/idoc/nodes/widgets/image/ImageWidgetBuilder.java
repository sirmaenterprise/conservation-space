package com.sirma.sep.content.idoc.nodes.widgets.image;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link ImageWidget} that represents a basic server side implementation of the image widget.
 * 
 * @author A. Kunchev
 */
public class ImageWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && ImageWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new ImageWidget(element);
	}

}
