package com.sirma.sep.content.idoc.extensions.widgets.process;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Base revert handler for {@link com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget}.
 * Here we remove specific saved BPMN id and activity json object so it will be fetched again.
 *
 * @author hlungov
 */
public class ProcessWidgetRevertHandler extends AbstractWidgetRevertHandler<ProcessWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ProcessWidget;
	}


	@Override
	public HandlerResult handle(ProcessWidget node, HandlerContext context) {
		node.getConfiguration().getConfiguration().remove(ProcessWidget.BPMN);
		node.getConfiguration().getConfiguration().remove(ProcessWidget.ACTIVITY);
		return super.handle(node, context);
	}
}
