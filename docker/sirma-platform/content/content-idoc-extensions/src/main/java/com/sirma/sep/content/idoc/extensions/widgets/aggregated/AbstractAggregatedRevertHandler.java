package com.sirma.sep.content.idoc.extensions.widgets.aggregated;

import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;

/**
 * /**
 * Base revert handler for Widgets with aggregated data. The only thing that this handler does is to remove the
 * additional property that is set in the widget configuration by {@link AbstractAggregatedVersionHandler}.
 *
 * @param <W>
 *            the type of the widget
 *
 * @author hlungov
 */
public abstract class AbstractAggregatedRevertHandler<W extends Widget> extends AbstractWidgetRevertHandler<W> {

	@Override
	public HandlerResult handle(W node, HandlerContext context) {
		node.getConfiguration().getConfiguration().remove(AbstractAggregatedVersionHandler.VERSION_DATA_CONFIG_KEY);
		return new HandlerResult(node);
	}
}
