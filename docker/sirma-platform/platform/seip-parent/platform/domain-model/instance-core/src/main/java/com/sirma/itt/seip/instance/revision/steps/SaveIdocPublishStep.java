package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;

import java.util.function.Consumer;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidget;
import com.sirma.sep.content.idoc.nodes.widgets.image.ImageWidgetConfiguration;

/**
 * Saves the view for the newly created revision. Before that generate new ids for the widgets and sections
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 110)
public class SaveIdocPublishStep implements PublishStep {

	@Override
	public String getName() {
		return Steps.SAVE_REVISION_VIEW.getName();
	}

	@Override
	public void execute(PublishContext publishContext) {
		Idoc view = publishContext.getView();
		view.generateNewIds();
		view.widgets()
			.filter(widget -> widget instanceof ImageWidget)
					.forEach(lockImageWidget());

		// the view content will be saved when the revision is saved
		publishContext.getRevision().add(TEMP_CONTENT_VIEW, view.asHtml());
	}

	private static Consumer<Widget> lockImageWidget() {
		return widget -> {
			ImageWidgetConfiguration configuration = widget.getConfiguration();
			configuration.lockWidget();
		};
	}

}
