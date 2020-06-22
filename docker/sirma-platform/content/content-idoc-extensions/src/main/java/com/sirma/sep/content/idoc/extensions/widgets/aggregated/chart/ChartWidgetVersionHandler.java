package com.sirma.sep.content.idoc.extensions.widgets.aggregated.chart;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.aggregated.AbstractAggregatedVersionHandler;
import com.sirma.sep.content.idoc.nodes.widgets.chart.ChartWidget;

/**
 *
 * Base version handler for {@link ChartWidget}. This handler uses the results retrieved by the
 * {@link ChartWidgetSearchHandler}. It will extract the id of the instances displayed in the widget, convert them
 * in version ids and store them back in the result map. The results from the handlers processing are stored in the
 * widget configuration as new property under specific key. This property is then used to load the version data for the
 * widget, when specific instance version is loaded/opened. <br />
 * If there are no results from the {@link ChartWidgetSearchHandler}, by default the handler will store empty object
 * for the version property.
 * <p>
 * <b>NOTE - the additional version property for this widget should not be used for any other reason, but storing
 * version data!</b>
 *
 * @author hlungov
 */
public class ChartWidgetVersionHandler extends AbstractAggregatedVersionHandler<ChartWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ChartWidget;
	}

}
