package com.sirma.sep.content.idoc.extensions.widgets.datatable;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidget;

/**
 * Base search handler for {@link DataTableWidget}. It will parse the search criteria from the widget configuration and
 * execute search with it. As result it will return the ids of the found instances or empty collection, if no instances
 * are found. If the widget selection mode is 'manually', no search will be performed, instead of that, the ids will be
 * retrieved and returned from the properties for selected objects. Uses the default implementation from
 * {@link AbstractWidgetSearchHandler}.
 *
 * @author BBonev
 */
public class DataTableWidgetSearchHandler extends AbstractWidgetSearchHandler<DataTableWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof DataTableWidget;
	}

}
