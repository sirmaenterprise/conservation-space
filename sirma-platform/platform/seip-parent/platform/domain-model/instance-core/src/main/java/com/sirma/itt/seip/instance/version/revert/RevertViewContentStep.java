package com.sirma.itt.seip.instance.version.revert;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TEMP_CONTENT_VIEW;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.handler.RevertContentNodeHandler;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;

/**
 * Reverts view content of the version so that it could replace the content of the current instance. This step uses
 * {@link RevertContentNodeHandler}s to restore the widgets to the state before they were versioned.<br>
 * When the content processing is done, the result content is stored in the properties model of the reverted instance
 * under temporary property key {@link DefaultProperties#TEMP_CONTENT_VIEW}. When the instance is saved the content will
 * be extracted and stored.<br>
 * The content is handled this way, so that we could avoid any changes to the current instance to the moment, when the
 * new data should be persisted/saved. Also this way we could guarantee that there will be no changes to the view of the
 * current instance, before the revert is fully completed by saving the replaced data.
 *
 * @author A. Kunchev
 */
@Extension(target = RevertStep.EXTENSION_NAME, enabled = true, order = 40)
public class RevertViewContentStep implements RevertStep {

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getName() {
		return "revertViewContent";
	}

	@Override
	public void invoke(RevertContext context) {
		Serializable versionId = context.getVersionId();
		ContentInfo versionView = instanceContentService.getContent(versionId, Content.PRIMARY_VIEW);
		if (!versionView.exists()) {
			throw new ContentNotFoundRuntimeException(
					"The view content of the instance - [" + versionId + "] does not exist.");
		}

		Idoc idoc = getIdoc(versionView);
		RevertContentNodeHandler.handle(idoc.widgets(), new HandlerContext());
		// setting the content into the instance so it could be processed as normal content passed through the client.
		// Also this way we prevent any problems in case of another step failure.
		// Check instance save steps
		context.getRevertResultInstance().add(TEMP_CONTENT_VIEW, idoc.asHtml());
	}

	private static Idoc getIdoc(ContentInfo versionView) {
		try {
			return Idoc.parse(versionView.getInputStream());
		} catch (IOException e) {
			throw new EmfRuntimeException(
					"Failed to parse the content for view with id - " + versionView.getContentId(), e);
		}
	}

}
