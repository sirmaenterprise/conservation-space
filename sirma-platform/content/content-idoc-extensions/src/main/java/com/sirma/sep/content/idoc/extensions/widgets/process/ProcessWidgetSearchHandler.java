package com.sirma.sep.content.idoc.extensions.widgets.process;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget;

/**
 * Base search handler for {@link com.sirma.sep.content.idoc.nodes.widgets.process.ProcessWidget}.
 *
 * @author hlungov
 */
public class ProcessWidgetSearchHandler extends AbstractWidgetSearchHandler<ProcessWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ProcessWidget;
	}
}
