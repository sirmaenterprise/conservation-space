package com.sirma.sep.content.idoc.extensions.widgets.image;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetSearchHandler;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;

/**
 * Base search handler for {@link ImageWidget}. The search criteria is retrieved from the widget configuration, if the
 * displayed object is not the current one. When the search is executed with the parsed criteria, the result is is
 * returned as collection of ids of the found instances. If no instance is found the result will be <code>null</code>.
 * The result could be also <code>null</code>, when for display is selected current object.
 *
 * @author A. Kunchev
 */
public class ImageWidgetSearchHandler extends AbstractWidgetSearchHandler<ImageWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ImageWidget;
	}

}
