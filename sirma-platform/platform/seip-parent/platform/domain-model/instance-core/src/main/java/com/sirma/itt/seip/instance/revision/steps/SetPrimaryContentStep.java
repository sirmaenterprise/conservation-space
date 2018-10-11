package com.sirma.itt.seip.instance.revision.steps;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

import javax.inject.Inject;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

@Extension(target = PublishStep.EXTENSION_NAME, order = 102.2)
public class SetPrimaryContentStep implements PublishStep {

	@Inject
	private InstanceContentService contentService;

	@Override
	public String getName() {
		return Steps.SET_PRIMARY_CONTENT.getName();
	}

	@Override
	public void execute(PublishContext publishContext) {
		ContentInfo content = contentService.getContent(publishContext.getRequest().getContentIdToPublish(),
														Content.PRIMARY_CONTENT);
		if (!content.exists()) {
			throw new RollbackedRuntimeException("Could not get uploaded content for the new revision");
		}

		Instance revision = publishContext.getRevision();
		revision.add(PRIMARY_CONTENT_ID, content.getContentId());
		revision.add(CONTENT_LENGTH, content.getLength());
		revision.add(NAME, content.getName());
		revision.add(MIMETYPE, content.getMimeType());
	}
}
