package com.sirma.itt.seip.instance.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.content.publish.UploadRevisionRequest;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Rest service that adds or updates content via instances end point
 *
 * @author BBonev
 */
@Path("/instances")
@ApplicationScoped
@Produces({ Versions.V2_JSON, MediaType.APPLICATION_JSON })
public class InstanceContentUploadRest {

	@Inject
	private ContentUploader contentUploader;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private Actions actions;

	/**
	 * Upload content to instance and the specified purpose. The method will add new primary content if not exists or
	 * will update existing one. The target content purpose could be overrided by passing the {@code purpose} parameter
	 *
	 * @param uploadRequest the upload request to parse to extract the uploaded file from
	 * @param instanceId the instance id that needs it's content updated. The instance should exists before calling this method
	 * otherwise {@link ResourceNotFoundException} (HTTP 404) will be thrown.
	 * @param purpose the purpose of the content assigned to the given instance
	 * @return the content info of the added/updated content
	 */
	@POST
	@Path("/{id}/content")
	@Transactional
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ContentInfo uploadContentToInstance(UploadRequest uploadRequest, @PathParam(JsonKeys.ID) String instanceId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam("purpose") String purpose) {
		InstanceType instanceType = instanceTypeResolver
				.resolve(instanceId)
					.orElseThrow(() -> new ResourceNotFoundException(instanceId));
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.SEMANTIC_TYPE, instanceType.getId());
		// We upload it without instance in order to assign the instance id inside {@link AssignPrimaryContentStep}
		// this is done because the save process my fail for some reason. Fixes CMF-26177
		return contentUploader.uploadForInstance(uploadRequest, instance, purpose, instanceType.isVersionable());
	}

	@POST
	@Path("/{id}/revision")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Instance uploadRevisionToInstance(UploadRequest uploadRequest, @PathParam(JsonKeys.ID) String instanceId) {
		UploadRevisionRequest revisionRequest = new UploadRevisionRequest(uploadRequest, resolveContentPurpose(uploadRequest));
		revisionRequest.setTargetId(instanceId);
		revisionRequest.setUserOperation(resolveUserOperation(uploadRequest));
		return (Instance) actions.callAction(revisionRequest);
	}

	private static String resolveUserOperation(UploadRequest uploadRequest) {
		return uploadRequest.resolveFormField(JsonKeys.USER_OPERATION, UploadRevisionRequest.OPERATION_NAME);
	}

	private static String resolveContentPurpose(UploadRequest uploadRequest) {
		return uploadRequest.resolveFormField("purpose", Content.PRIMARY_CONTENT);
	}
}
