package com.sirma.sep.content.idoc.extensions.widgets.contentviewer;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.content.ContentViewerWidget;

/**
 * Base search handler for {@link ContentViewerWidget}. It will retrieve the configuration of the widget, parse the
 * search criteria and execute it. As result it will return the ids of the found instances. If the widget selection mode
 * is set 'manually', the selected objects from the widget configuration will be returned as result and no search will
 * be performed. If no instances are found it will return <code>null</code> as result. Uses the base implementation from
 * the {@link AbstractWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class ContentViewerWidgetSearchHandler extends AbstractWidgetSearchHandler<ContentViewerWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ContentViewerWidget;
	}

}
