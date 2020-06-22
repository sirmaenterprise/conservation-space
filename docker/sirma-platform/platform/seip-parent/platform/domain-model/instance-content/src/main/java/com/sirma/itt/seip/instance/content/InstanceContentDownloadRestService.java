package com.sirma.itt.seip.instance.content;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.sep.content.Content.PRIMARY_CONTENT;
import static com.sirma.sep.content.Content.PRIMARY_VIEW;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.rest.ContentDownloadService;

/**
 * Content service that provides instance content for download. The service supports Range HTTP header and partial
 * download.
 *
 * @author BBonev
 */
@Path("/instances")
@ApplicationScoped
public class InstanceContentDownloadRestService {

	private static final String PURPOSE = "purpose";

	private static final String DOWNLOAD = "download";

	@Inject
	private ContentDownloadService downloadService;

	@Inject
	private InstanceAccessEvaluator accessEvaluator;

	/**
	 * Gets the content head response for the requested instance content.
	 *
	 * @param instanceId
	 *            the instance id that needs it's content downloaded
	 * @param purpose
	 *            the content purpose related to the instance. Optional query parameter if not specified then
	 *            {@value Content#PRIMARY_CONTENT} will be used
	 * @return response containing the supported content range and content type as HTTP headers if the content is found
	 */
	@HEAD
	@Path("{id}/content")
	public Response getContentHead(@PathParam(KEY_ID) String instanceId, @QueryParam(PURPOSE) String purpose) {
		if (!accessEvaluator.canRead(instanceId)) {
			throw new NoPermissionsException(instanceId, "No permission to access instance's content");
		}

		return downloadService.getContentHeadResponse(instanceId, defaultIfBlank(purpose, PRIMARY_CONTENT));
	}

	/**
	 * Streams the instance content or gets it for download if the query parameter {@code download} is present
	 *
	 * @param instanceId
	 *            the instance id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional query parameter if not specified
	 *            default content purpose will be used: {@value Content#PRIMARY_CONTENT}
	 * @param forDownload
	 *            if present the content will have Content-Disposition header to notify the browser to open save dialog
	 *            to download the file to the user file system.
	 * @param range
	 *            the HTTP header Range that specifies the range of the content to return. The default value is all
	 *            content
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@Path("{id}/content")
	public void streamContent(@PathParam(KEY_ID) String instanceId, @QueryParam(PURPOSE) String purpose,
			@QueryParam(DOWNLOAD) String forDownload,
			@DefaultValue(Range.DEFAULT_RANGE) @HeaderParam(Range.HEADER) Range range,
			@Context HttpServletResponse response) {
		if (!accessEvaluator.canRead(instanceId)) {
			throw new NoPermissionsException(instanceId, "No permission to access instance's content");
		}

		downloadService.sendContent(instanceId, defaultIfBlank(purpose, PRIMARY_CONTENT), range, forDownload != null,
				response, null);
	}

	/**
	 * Streams the instance view
	 *
	 * @param instanceId
	 *            the instance id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional query parameter if not specified
	 *            default content purpose will be used: {@value Content#PRIMARY_VIEW}
	 * @param forDownload
	 *            if present the content will have Content-Disposition header to notify the browser to open save dialog
	 *            to download the file to the user file system.
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@Path("{id}/view")
	public void streamView(@PathParam(KEY_ID) String instanceId, @QueryParam(PURPOSE) String purpose,
			@QueryParam(DOWNLOAD) String forDownload, @Context HttpServletResponse response) {
		if (!accessEvaluator.canRead(instanceId)) {
			throw new NoPermissionsException(instanceId, "No permission to access instance's view");
		}

		downloadService.sendContent(instanceId, defaultIfBlank(purpose, PRIMARY_VIEW), Range.ALL, forDownload != null,
				response, null);
	}

	/**
	 * Streams the instance content preview
	 *
	 * @param instanceId
	 *            the instance id to fetch the content preview to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is
	 *            optional query parameter if not specified default content
	 *            purpose will be used: {@value Content#PRIMARY_CONTENT}
	 * @param range
	 *            the HTTP header Range that specifies the range of the content
	 *            to return. The default value is all content
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@Path("{id}/content/preview")
	public void streamPreview(@PathParam(KEY_ID) String instanceId, @QueryParam(PURPOSE) String purpose,
			@DefaultValue(Range.DEFAULT_RANGE) @HeaderParam(Range.HEADER) Range range,
			@Context HttpServletResponse response) {
		if (!accessEvaluator.canRead(instanceId)) {
			throw new NoPermissionsException(instanceId, "No permission to access instance's preview");
		}

		downloadService.sendPreview(instanceId, defaultIfBlank(purpose, PRIMARY_CONTENT), response, range);
	}
}