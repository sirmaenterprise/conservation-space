package com.sirmaenterprise.sep.content.idoc.extensions.widgets.objectdata;

import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirmaenterprise.sep.content.idoc.nodes.widgets.objectdata.ObjectDataWidget;

/**
 * Base version handler for {@link ObjectDataWidget}. It will retrieved the ids of the instances that are shown in the
 * widget, convert them in to version ids and store them in the widget configuration. Uses default handle implementation
 * from the super class to complete this process.
 *
 * @author A. Kunchev
 */
public class ObjectDataWidgetVersionHandler extends AbstractWidgetVersionHandler<ObjectDataWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ObjectDataWidget;
	}

}
