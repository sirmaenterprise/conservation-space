package com.sirma.sep.content.idoc.extensions.widgets.aggregated.chart;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.AbstractAggregatedRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidget;

/**
 * Base revert handler for {@link ChartWidget}.
 *
 * @author hlungov
 */
public class ChartWidgetRevertHandler extends AbstractAggregatedRevertHandler<ChartWidget> {


	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ChartWidget;
	}

}
