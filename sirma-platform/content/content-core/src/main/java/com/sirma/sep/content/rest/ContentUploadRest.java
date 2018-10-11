package com.sirma.sep.content.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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

	/**
	 * Upload single file from multi part form data
	 *
	 * @param uploadRequest
	 *            the upload request
	 * @param purpose
	 *            the purpose
	 * @return a content info containing information for the uploaded file
	 */
	@POST
	@Transactional
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ContentInfo addContent(UploadRequest uploadRequest,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam("purpose") String purpose) {

		return contentUploader.uploadWithoutInstance(uploadRequest, purpose);
	}
}
