package com.sirma.sep.content.idoc.extensions.widgets.objectlink;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.insertlink.ObjectLinkWidget;

/**
 * Base search handler for {@link ObjectLinkWidget}. This handler will extract the selected object from the widget
 * configuration and store them as search results.<br>
 * At the moment this widget works only in 'manually' mode so we do not need to execute additional searches to get the
 * displayed objects in the widget as they are already stored in the 'selectedObject' section.
 *
 * @author A. Kunchev
 */
public class ObjectLinkWidgetSearcHandler extends AbstractWidgetSearchHandler<ObjectLinkWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ObjectLinkWidget;
	}

	@Override
	public HandlerResult handle(ObjectLinkWidget node, HandlerContext context) {
		return collectSelectedObjectsAndStoreThemAsResults(node);
	}

}
