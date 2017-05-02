package com.sirma.itt.seip.instance.content;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.upload.ContentUploader;
import com.sirma.itt.seip.content.upload.UploadRequest;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

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

	/**
	 * Upload content to instance and the specified purpose. The method will add new primary content if not exists or
	 * will update existing one.
	 *
	 * @param uploadRequest
	 *            the upload request
	 * @param instanceId
	 *            the instance id
	 * @param purpose
	 *            the purpose
	 * @return the content info of the added/updated content
	 */
	@POST
	@Path("/{id}/content")
	@Transactional(TxType.REQUIRED)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ContentInfo uploadContentToInstance(UploadRequest uploadRequest, @PathParam(JsonKeys.ID) String instanceId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam("purpose") String purpose) {

		Optional<InstanceReference> reference = instanceTypeResolver.resolveReference(instanceId);
		if (!reference.isPresent()) {
			throw new ResourceNotFoundException(instanceId);
		}
		boolean createVersion = reference.get().getType().isVersionable();
		return contentUploader.uploadForInstance(uploadRequest, instanceId, purpose, createVersion);
	}
}
