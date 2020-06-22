package com.sirma.sep.content.idoc.extensions.widgets.image;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.extensions.widgets.AbstractWidgetRevertHandler;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;

/**
 * Base revert handler for {@link ImageWidget}. The handler will get the stored identifiers for the selected objects,
 * convert them back to the identifiers that point to the current objects instead of versions and stores them back in
 * the configuration. Uses the super implementation for the base logic. In addition the image widget is unlocked,
 * because when the version for it is create, the widget is locked.
 *
 * @author A. Kunchev
 * @see ImageWidgetVersionHandler
 */
public class ImageWidgetRevertHandler extends AbstractWidgetRevertHandler<ImageWidget> {

	@Override
	public boolean accept(ContentNode node) {
		return node instanceof ImageWidget;
	}

}
