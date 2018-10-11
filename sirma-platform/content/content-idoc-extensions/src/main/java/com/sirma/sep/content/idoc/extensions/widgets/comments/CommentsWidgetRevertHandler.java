package com.sirma.sep.content.idoc.extensions.widgets.comments;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.comments.CommentsWidget;

/**
 * Base revert handler for {@link CommentsWidget}. This handler restores the configuration for the 'filterCriteria' that
 * is stored in the widget configuration. Through this property the widget is limited to search only results for given
 * time period. This limit is set by {@link CommentsWidgetVersionHandler}, when the widget is processed for versioning.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetRevertHandler extends AbstractWidgetRevertHandler<CommentsWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof CommentsWidget;
	}

	@Override
	public HandlerResult handle(CommentsWidget node, HandlerContext context) {
		// clear the one added by the version handler
		// if there was another value it will be reverted in the abstract handler
		node.getConfiguration().addNotNullProperty("filterCriteria", new JsonObject());
		return super.handle(node, context);
	}

}
