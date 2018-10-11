package com.sirma.sep.content.idoc.extensions.widgets.datatable;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidget;

/**
 * Base version handler for {@link DataTableWidget}. It will retrieved the ids of the instances that are shown in the
 * widget, convert them in to version ids and store them in the widget configuration. Uses default handle implementation
 * from the super class to complete this process.
 *
 * @author A. Kunchev
 */
public class DataTableWidgetVersionHandler extends AbstractWidgetVersionHandler<DataTableWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof DataTableWidget;
	}

}
