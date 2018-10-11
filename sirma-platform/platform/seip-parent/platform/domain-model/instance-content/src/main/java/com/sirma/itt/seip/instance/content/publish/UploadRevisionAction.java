package com.sirma.itt.seip.instance.content.publish;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.upload.ContentUploader;

/**
 * Action that instance publish with user uploaded content.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 25/06/2018
 */
@Extension(target = Action.TARGET_NAME, order = 350)
public class UploadRevisionAction implements Action<UploadRevisionRequest> {

	private ContentUploader contentUploader;
	private RevisionService revisionService;
	private InstanceContentService instanceContentService;

	@Inject
	public UploadRevisionAction(ContentUploader contentUploader,
			RevisionService revisionService, InstanceContentService instanceContentService) {
		this.contentUploader = contentUploader;
		this.revisionService = revisionService;
		this.instanceContentService = instanceContentService;
	}

	@Override
	public Object perform(UploadRevisionRequest request) {
		InstanceReference reference = request.getTargetReference();
		if (reference == null) {
			throw new InstanceNotFoundException(request.getTargetId());
		}

		Instance instanceToPublish = reference.toInstance();

		ContentInfo content = null;
		try {
			if (instanceToPublish.isUploaded()) {
				content = contentUploader.uploadForInstance(request.getUploadRequest(), reference.getId(),
						request.getContentPurpose(), true);
				// synchronizes instance properties with new content before publish action to be executed.
				instanceToPublish.add(DefaultProperties.NAME, content.getName());
				instanceToPublish.add(DefaultProperties.MIMETYPE, content.getMimeType());
				instanceToPublish.add(DefaultProperties.CONTENT_LENGTH, content.getLength());

				return revisionService.publish(
						new PublishInstanceRequest(instanceToPublish, new Operation("uploadRevision", true), null,
								null));
			} else {
				content = contentUploader.uploadWithoutInstance(request.getUploadRequest(), request.getContentPurpose());
				return revisionService.publish(
						new PublishInstanceRequest(instanceToPublish, new Operation("uploadRevision", true), null, null)
								.withContentIdToPublish(content.getContentId()));
			}
		} catch (RuntimeException e) {
			if (content != null && StringUtils.isNotBlank(content.getContentId())) {
				instanceContentService.deleteContent(content.getContentId(), request.getContentPurpose());
			}
			throw new ResourceException(Response.Status.INTERNAL_SERVER_ERROR,
					new ErrorData("Upload revision failed for " + reference.getId()), e);
		}
	}

	@Override
	public String getName() {
		return UploadRevisionRequest.OPERATION_NAME;
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(UploadRevisionRequest request) {
		return true;
	}
}
