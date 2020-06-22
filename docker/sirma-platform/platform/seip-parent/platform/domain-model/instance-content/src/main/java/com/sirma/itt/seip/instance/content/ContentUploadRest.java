package com.sirma.itt.seip.instance.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Rest service that handle post requests for new content upload and upload new version.
 *
 * @author BBonev
 * @author Vilizar Tsonev
 */
@Path("/content")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ContentUploadRest {

	@Inject
	private ContentUploader contentUploader;

	@Inject
	private CheckOutCheckInService checkOutCheckInService;

	/**
	 * Performs a check-in or plain update operation of the provided file. Its content is updated and the associated
	 * instance is unlocked, if it's been locked by the current user.
	 *
	 * @param uploadRequest
	 *            the upload request
	 * @param id
	 *            the content ID
	 * @param update
	 *            if present a generic content update will be performed
	 * @return a content info containing information for the uploaded file
	 */
	@POST
	@Transactional
	@Path("/{id}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ContentInfo updateContent(UploadRequest uploadRequest, @PathParam(JsonKeys.ID) String id,
			@QueryParam("update") String update) {
		if (update != null) {
			return contentUploader.updateWithoutInstance(id, uploadRequest);
		}
		Content content = contentUploader.buildContentFromRequest(uploadRequest);
		return checkOutCheckInService.checkIn(content, id);
	}
}
