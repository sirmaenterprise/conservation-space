/**
 *
 */
package com.sirma.itt.emf.web.rest;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.resources.downloads.DownloadsService;

/**
 * Contains rest services for processing the instances, which are added or removed from downloads. Uses DownloadsService
 * to add, remove and extract instances that can be marked for download for the user.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
@Path("/downloads")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DownloadsRestService extends EmfRestService {

	private static final String REQUIRED_ARGUMENT_IS_NOT_SET = "Required argument is not set.";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String FAILED_TO_ADD_TO_DOWNLOADS = "emf.rest.downloads.link.failed.creation";

	private static final String NO_INSTANCES_FOR_DOWNLOAD = "emf.rest.downloads.no.downloads";

	private static final String ARCHIVE_ID = "archiveId";

	@Inject
	private DownloadsService downloadsService;

	/**
	 * Gets the currently logged user instances marked for download. Uses DownloadsService to extract the collection of
	 * instances. After that the all the instances are transformed to JSON objects and added to the response.
	 *
	 * <pre>
	 * Service path example:
	 * GET emf/services/downloads
	 * </pre>
	 *
	 * @return <b>OK</b> response with message, that no instance are found for the current user, if the returned
	 *         collection from the service is empty <b>OR</b><br />
	 *         <b>OK</b> response with the instances collection as a JSON object, if there are instances for download
	 *         for the current user<br />
	 */
	@GET
	public Response getDownlaodsList() {
		Collection<InstanceReference> downloadsInstances = downloadsService.getAll();
		if (CollectionUtils.isEmpty(downloadsInstances)) {
			String noInstancesRawMsg = labelProvider.getValue(NO_INSTANCES_FOR_DOWNLOAD);
			String noInstancesMsg = String.format(noInstancesRawMsg, getCurrentLoggedUser().getDisplayName());
			return buildOkResponse(noInstancesMsg);
		}
		JSONArray response = convertInstanceReferencesToJSON(downloadsInstances);

		return buildOkResponse(response.toString());
	}

	/**
	 * Adds instance to currently logged user downloads list. Uses DownloadsService to pass the instance, that will be
	 * added. The id and the type of the instance are passed as data parameter, so they can be used to load the
	 * instance. This service consumes POST request, if the id or the type are missing, it will return bad response.
	 *
	 * <pre>
	 * Service path example:
	 * POST emf/services/downloads
	 * data = {
	 *     instanceId   : 'emf:xxxxxxxx-xxxxxxxx-xxxx-xxxxxxxxxxxx',
	 *     instanceType : 'someInstanceType'
	 * }
	 * </pre>
	 *
	 * @param data
	 *            the request data for the service
	 * @return <b>OK</b> response - when the instance is successfully added to the current user downloads list.<br />
	 *         <b>BAD</b> response - when the instance id or type are missing or when the operation fails.
	 */
	@POST
	public Response addToDownloads(String data) {
		InstanceReference instanceReference = extractInstanceReference(data);

		if (instanceReference == null) {
			throw new RestServiceException(EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS, Status.BAD_REQUEST);
		}

		boolean isAdded = downloadsService.add(instanceReference);

		if (isAdded) {
			return buildOkResponse(null);
		}

		String errorMessage = labelProvider.getValue(FAILED_TO_ADD_TO_DOWNLOADS);
		String formatedMessage = String.format(errorMessage, instanceReference.getIdentifier());
		return buildOkResponse(formatedMessage);
	}

	/**
	 * Removes instance from currently logged user downloads list. Uses DownloadsService to pass the instance, that will
	 * be removed. The id and the type of the instance are passed as a data parameter, so they can be used to load the
	 * instance. This service consumes DELETE request, if the id or type are missing, it will return bad response.
	 *
	 * <pre>
	 * Service path example:
	 * DELETE emf/services/downloads
	 * data = {
	 *     instanceId   : 'emf:xxxxxxxx-xxxxxxxx-xxxx-xxxxxxxxxxxx',
	 *     instanceType : 'someInstanceType'
	 * }
	 * </pre>
	 *
	 * @param data
	 *            the request data for the service
	 * @return <b>OK</b> response - when the instance is successfully removed from the current user downloads.<br />
	 *         <b>BAD</b> response - when the instance id or type are missing.
	 *         <p />
	 *         <b>NOTE! The service will return always response 'OK', regardless the operation result.</b>
	 */
	@DELETE
	public Response removeFromDownloads(String data) {
		InstanceReference instanceReference = extractInstanceReference(data);

		if (instanceReference == null) {
			throw new RestServiceException(EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS, Status.BAD_REQUEST);
		}

		downloadsService.remove(instanceReference);
		return buildOkResponse(null);
	}

	/**
	 * Removes all instances from the currently logged user downloads list.
	 *
	 * <pre>
	 * Service path example:
	 * DELETE emf/services/downloads/all
	 * </pre>
	 *
	 * @return <b>OK</b> response, regardless the operation result.
	 */
	@Path("all")
	@DELETE
	public Response removeAll() {
		boolean removed = downloadsService.removeAll();

		if (removed) {
			LOGGER.debug("All documents for downloads were unmarked.");
		} else {
			LOGGER.debug("There are no marked documents or there is a porblem with the service call.");
		}

		return buildOkResponse(null);
	}

	/**
	 * Creates archive from documents marked for download for the current user.
	 *
	 * <pre>
	 * Service path example:
	 * POST emf/services/downloads/zip
	 * </pre>
	 *
	 * @return <b>OK</b> response - when the archive is successfully created. The response body contains the JSON object
	 *         with the archive id.<br />
	 *         <b>BAD</b> response - when there are problems with DMS request.
	 */
	@Path("zip")
	@POST
	public Response createArchive() {
		try {
			String result = downloadsService.createArchive();
			return buildOkResponse(result);
		} catch (RuntimeException e) {
			LOGGER.debug("DMS error while trying to create archive {}", e.getMessage(), e);
			throw new RestServiceException("There was a problem with the DMS rest call for creating archive.",
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Gets the status of the archive.
	 *
	 * <pre>
	 * Service path example:
	 * GET emf/services/downloads/zip/{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}/status
	 *
	 * <b>Response example:</b>
	 * {
	 *  "status": "IN_PROGRESS",
	 *  "done": "4371",
	 *  "total": "6245",
	 *  "filesAdded": "4",
	 *  "totalFiles": "6"
	 * }
	 *
	 * <b>Possible statuses:</b>
	 * PENDING                   - The archiving hasn't started yet.
	 * IN_PROGRESS               - The archive is not ready for download.
	 * DONE                      - The archiving is complete and the archive can be downloaded.
	 * MAX_CONTENT_SIZE_EXCEEDED - The file size is too large to be zipped up.
	 * CANCELLED                 - The archiving was stopped or the archive was deleted.
	 * </pre>
	 *
	 * @param archiveId
	 *            the DMS id of the archive, which status will be checked
	 * @return <b>OK</b> response - when the archive status is successfully extracted. The response body contains the
	 *         JSON object with the archive status.<br />
	 *         <b>BAD</b> response - when there are problems with DMS request or request parameter is not set.
	 */
	@Path("zip/{archiveId}/status")
	@GET
	public Response getArchiveStatus(@PathParam(ARCHIVE_ID) String archiveId) {
		if (StringUtils.isNullOrEmpty(archiveId)) {
			throw new RestServiceException(REQUIRED_ARGUMENT_IS_NOT_SET, Status.BAD_REQUEST);
		}

		try {
			String status = downloadsService.getArchiveStatus(archiveId);
			return buildOkResponse(status);
		} catch (RuntimeException e) {
			LOGGER.debug("DMS error while checking archive status {}", e.getMessage(), e);
			throw new RestServiceException("There was a problem with the DMS rest call for getting the archive status.",
					Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Gets the URL for the completed archive, from which the archive can be downloaded. This service should be called
	 * only, when the status of the archive is "DONE", which means that the archivation is complete.
	 *
	 * <pre>
	 * Service path example:
	 * GET emf/services/downloads/zip/{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}
	 *
	 * <b>Response example:</b>
	 * {
	 *  "link":"http://xxx.xxx.xxx.xxx:XXXX/alfresco/service/api/node/content/workspace/SpacesStore/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/generated-name.zip"
	 * }
	 * </pre>
	 *
	 * @param archiveId
	 *            the DMS id of the archive, which download URL will be returned
	 * @return <b>OK</b> response - when the archive download URL is successfully extracted. The response body contains
	 *         the JSON object with the archive download URL.<br />
	 *         <b>BAD</b> response - when there are problems with DMS request or JSON building, and request parameter is
	 *         not set.
	 */
	@Path("zip/{archiveId}")
	@GET
	public Response getArchive(@PathParam(ARCHIVE_ID) String archiveId) {
		if (StringUtils.isNullOrEmpty(archiveId)) {
			throw new RestServiceException(REQUIRED_ARGUMENT_IS_NOT_SET, Status.BAD_REQUEST);
		}

		try {
			String archiveLink = downloadsService.getArchiveLink(archiveId);
			return buildOkResponse(new JSONObject().append("link", archiveLink).toString());
		} catch (RuntimeException eDMS) {
			LOGGER.debug("DMS error while fetching archive {}", eDMS.getMessage(), eDMS);
			throw new RestServiceException("There was a problem with the DMS rest call for getting the archive URL.",
					Status.INTERNAL_SERVER_ERROR);
		} catch (JSONException eJSON) {
			LOGGER.debug("There was a problem JSON object.", eJSON);
			throw new RestServiceException("There was a problem JSON object.", Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Removes given archive from DMS. This service should be called after the archive processing is done and there is
	 * no need for it any more. The service can be used to stop the archive creation or to delete already created
	 * archive.
	 *
	 * <pre>
	 * Service path example: DELETE emf/services/downloads/zip/{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}
	 *
	 * @param archiveId
	 * @return <b>OK</b> response - when the archive is removed or stopped successfully. <br />
	 *         <b>BAD</b> response - when there are problems with DMS request or the request parameter is not set.
	 */
	@DELETE
	@Path("zip/{archiveId}")
	public Response removeArchive(@PathParam(ARCHIVE_ID) String archiveId) {
		if (StringUtils.isNullOrEmpty(archiveId)) {
			throw new RestServiceException(REQUIRED_ARGUMENT_IS_NOT_SET, Status.BAD_REQUEST);
		}

		try {
			String result = downloadsService.removeArchive(archiveId);
			return buildOkResponse(result.replace(",", ""));
		} catch (RuntimeException e) {
			LOGGER.debug("DMS error while removing archive {}", e.getMessage(), e);
			throw new RestServiceException("There was a problem with the DMS rest call for removing the archive.",
					Status.INTERNAL_SERVER_ERROR);
		}

	}

}
