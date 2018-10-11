package com.sirma.sep.content.idoc.extensions.widgets.contentviewer;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.nodes.widgets.content.ContentViewerWidget;

/**
 * Base version handler for {@link ContentViewerWidget}. It will retrieved the ids of the instances that are shown in
 * the widget, convert them in to version ids and store them in the widget configuration. Uses default handle
 * implementation from the super class to complete this process.
 *
 * @author A. Kunchev
 */
public class ContentViewerWidgetVersionHandler extends AbstractWidgetVersionHandler<ContentViewerWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ContentViewerWidget;
	}

}
