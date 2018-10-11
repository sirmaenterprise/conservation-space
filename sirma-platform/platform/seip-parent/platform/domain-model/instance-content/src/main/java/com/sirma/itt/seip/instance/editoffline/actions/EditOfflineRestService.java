package com.sirma.itt.seip.instance.editoffline.actions;

import static com.sirma.itt.seip.instance.editoffline.updaters.AbstractMSOfficeCustomPropertyUpdater.VERSION;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.editoffline.exception.FileCustomPropertiesUpdateException;
import com.sirma.itt.seip.instance.editoffline.exception.FileNotSupportedException;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Executes the download action for edit offline of uploaded content.
 *
 * @author T. Dossev
 */
@Path("/instances")
@ApplicationScoped
public class EditOfflineRestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Actions actions;

	/**
	 * Streams the uploaded content of an instance for offline edit.
	 *
	 * @param request
	 *            {@link EditOfflineCheckOutRequest} object which contains the information needed to execute the action
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/{id}/actions/edit-offline-check-out")
	public void streamContent(EditOfflineCheckOutRequest request) {
		try {
			actions.callAction(request);
		} catch (FileCustomPropertiesUpdateException | FileNotSupportedException e) {
			LOGGER.trace("", e);
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * Performs a check-in or plain update operation of the provided file. Its content is updated and the associated
	 * instance is unlocked, if it meets all validation requirements.
	 *
	 * @param uploadRequest
	 *            the upload request
	 * @param targetId
	 *            the updated instance ID
	 * @param version
	 *            the affected instance version
	 * @return a content info containing information for the uploaded file
	 */
	@POST
	@Path("/{id}/actions/edit-offline-check-in")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public ContentInfo updateContent(UploadRequest uploadRequest, @PathParam(RequestParams.KEY_ID) String targetId,
			@QueryParam(VERSION) String version) {
		uploadRequest.setTargetId(targetId);
		uploadRequest.setInstanceVersion(version);
		return (ContentInfo) actions.callAction(uploadRequest);
	}

}
