package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentImport;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Publish step that clones the uploaded content of an instance to it's revision
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 101)
public class CloneUploadedContent implements PublishStep {

	@Inject
	private InstanceContentService contentService;

	@Override
	public String getName() {
		return Steps.CLONE_UPLOADED_CONTENT.getName();
	}

	@Override
	public void execute(PublishContext context) {
		if (!context.getRequest().getInstanceToPublish().isUploaded()) {
			return;
		}
		Serializable instanceToPublishId = context.getRequest().getInstanceToPublish().getId();
		ContentInfo content = contentService.getContent(instanceToPublishId, Content.PRIMARY_CONTENT);
		if (content.exists()) {
			ContentImport revisionContent = ContentImport
					.copyFrom(content)
						.setInstanceId(context.getRevision().getId());
			String importedContentId = contentService.importContent(revisionContent);
			if (importedContentId == null) {
				throw new RollbackedRuntimeException("Could not copy primary content to the new revision");
			}
			// we do not check if the content exists because the file will be copied async after transaction success.
			context.getRevision().add(PRIMARY_CONTENT_ID, importedContentId);
		}
	}
}
