package com.sirma.sep.content.idoc.extensions.widgets.process;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetVersionHandler;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Base version handler for {@link com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget}.
 *
 * @author hlungov
 */
public class ProcessWidgetVersionHandler extends AbstractWidgetVersionHandler<ProcessWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ProcessWidget;
	}
}
