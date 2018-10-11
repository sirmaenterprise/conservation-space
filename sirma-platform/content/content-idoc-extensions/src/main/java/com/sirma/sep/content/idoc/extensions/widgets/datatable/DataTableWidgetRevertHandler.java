package com.sirma.sep.content.idoc.extensions.widgets.datatable;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.datatable.DataTableWidget;

/**
 * Base revert handler for {@link DataTableWidget}. The handler will get the stored identifiers for the selected
 * objects, convert them back to the identifiers that point to the current objects instead of versions and stores them
 * back in the configuration. Uses the super implementation, because there is no need for additional custom logic.
 *
 * @author A. Kunchev
 */
public class DataTableWidgetRevertHandler extends AbstractWidgetRevertHandler<DataTableWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof DataTableWidget;
	}

}
