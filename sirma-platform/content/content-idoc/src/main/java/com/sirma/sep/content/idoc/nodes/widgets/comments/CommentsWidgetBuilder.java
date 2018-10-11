package com.sirma.sep.content.idoc.nodes.widgets.comments;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link CommentsWidget} that represents a basic server side implementation of the comments widget.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME) && CommentsWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new CommentsWidget(element);
	}

}
