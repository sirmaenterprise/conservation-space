package com.sirma.sep.content.idoc.nodes.widgets.recentactivities;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Builder for the {@link RecentActivitiesWidget} that represents a basic server side implementation of the recent
 * activities widget.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasAttr(Widget.WIDGET_NAME)
				&& RecentActivitiesWidget.NAME.equals(element.attr(Widget.WIDGET_NAME));
	}

	@Override
	public ContentNode build(Element element) {
		return new RecentActivitiesWidget(element);
	}

}
