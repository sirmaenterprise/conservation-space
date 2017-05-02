package com.sirmaenterprise.sep.content.idoc.extensions.widgets.aggregatedtable;

import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Base revert handler for {@link AggregatedTableWidget}. The only thing that this handler does is to remove the
 * additional property that is set in the widget configuration by {@link AggregatedTableVersionHandler}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableRevertHandler extends AbstractWidgetRevertHandler<AggregatedTableWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof AggregatedTableWidget;
	}

	@Override
	public HandlerResult handle(AggregatedTableWidget node, HandlerContext context) {
		node.getConfiguration().getConfiguration().remove(AggregatedTableVersionHandler.VERSION_DATA_CONFIG_KEY);
		return new HandlerResult(node);
	}

}
