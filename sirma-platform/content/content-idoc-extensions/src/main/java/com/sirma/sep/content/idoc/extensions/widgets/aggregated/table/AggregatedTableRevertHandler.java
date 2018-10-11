package com.sirma.sep.content.idoc.extensions.widgets.aggregated.table;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.AbstractAggregatedRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.aggregatedtable.AggregatedTableWidget;

/**
 * Base revert handler for {@link AggregatedTableWidget}. The only thing that this handler does is to remove the
 * additional property that is set in the widget configuration by {@link AggregatedTableVersionHandler}.
 *
 * @author A. Kunchev
 */
public class AggregatedTableRevertHandler extends AbstractAggregatedRevertHandler<AggregatedTableWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof AggregatedTableWidget;
	}

}
