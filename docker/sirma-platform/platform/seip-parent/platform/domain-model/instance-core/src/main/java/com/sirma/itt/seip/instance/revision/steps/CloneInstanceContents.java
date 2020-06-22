package com.sirma.itt.seip.instance.revision.steps;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Publish step that clones the uploaded and ocr content of an instance to it's revision.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 101)
public class CloneInstanceContents implements PublishStep {
	private static final String OCR_CONTENT = "ocr";

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

		cloneContent(context.getRevision(), instanceToPublishId, Content.PRIMARY_CONTENT);
		cloneContent(context.getRevision(), instanceToPublishId, OCR_CONTENT);
	}

	private void cloneContent(Instance revision, Serializable instanceToPublishId, String contentPurpose) {
		ContentInfo content = contentService.getContent(instanceToPublishId, contentPurpose);
		if (content.exists()) {
			ContentImport contentImport = ContentImport.copyFrom(content).setInstanceId(revision.getId());
			String importedContentId = contentService.importContent(contentImport);
			if (importedContentId == null) {
				throw new RollbackedRuntimeException("Could not copy primary content to the new revision");
			}
			// we do not check if the content exists because the file will be copied async after transaction success.
			revision.add(contentPurpose, importedContentId);
		}
	}
}
