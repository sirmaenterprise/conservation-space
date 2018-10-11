package com.sirma.sep.content.idoc.extensions.widgets.objectdata;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidget;

/**
 * Base search handler for {@link ObjectDataWidget}. It will retrieve the configuration from the widget, parse the
 * search criteria and execute search with it. As result this handler will return the ids of the found instances or
 * <code>null</code>, if no instance is found with the extracted criteria. If the widget selection mode is set to
 * 'manually', the ids will be retrieved from the properties for selected objects and returned directly without
 * performing search. Uses the default implementation in {@link AbstractWidgetSearchHandler}.
 *
 * @author A. Kunchev
 */
public class ObjectDataWidgetSearchHandler extends AbstractWidgetSearchHandler<ObjectDataWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ObjectDataWidget;
	}

}
