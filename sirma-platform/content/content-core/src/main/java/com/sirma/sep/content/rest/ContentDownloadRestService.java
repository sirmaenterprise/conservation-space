package com.sirma.sep.content.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.rest.annotations.http.method.PROPFIND;
import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.sep.content.Content;

/**
 * Content service that provides content for download. The service supports Range HTTP header and partial download.
 *
 * @author BBonev
 */
@Path("/content")
@ApplicationScoped
public class ContentDownloadRestService {

	private static final String GET_AND_HEAD = "GET, HEAD";
	private static final String PURPOSE = "purpose";
	private static final String CONTENT_ID = "content-id";
	private static final String CONTENT_ID_PARAM = "{" + CONTENT_ID + "}";

	@Inject
	private ContentDownloadService downloadService;

	/**
	 * Gets the content info for the given content id and/or purpose.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional path parameter if not specified
	 *            default content purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @return the content info
	 */
	@HEAD
	@Path(CONTENT_ID_PARAM)
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public Response getContentHead(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose) {
		return downloadService.getContentHeadResponse(contentId, purpose);
	}

	/**
	 * Sends the content identified by the given content id and/or purpose to the caller. The method supports HTTP Range
	 * header for partial retrieve.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional path parameter if not specified
	 *            default content purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @param forDownload
	 *            if present the content will have Content-Disposition header to notify the browser to save the file
	 * @param fileName
	 *            if present the content will have Content-Disposition header to notify the browser to save the file
	 *            with the custom file name
	 * @param range
	 *            the HTTP header Range that specifies the range of the content to return. The default value is all
	 *            content
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@Path(CONTENT_ID_PARAM)
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public void streamContent(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose,
			@QueryParam("download") String forDownload, @QueryParam("fileName") String fileName,
			@DefaultValue(Range.DEFAULT_RANGE) @HeaderParam(Range.HEADER) Range range,
			@Context HttpServletResponse response) {
		downloadService.sendContent(contentId, purpose, range, forDownload != null, response, fileName);
	}

	/**
	 * Sends the content identified by the given content id and/or purpose to the caller. The method marks the content
	 * for cacheable and valid for a single day.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional path parameter if not specified
	 *            default content purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @return A method not allowed response with allow header of GET and HEAD
	 */
	@PROPFIND
	@SuppressWarnings("static-method")
	@Path("static/" + CONTENT_ID_PARAM)
	@Produces(MediaType.APPLICATION_XML)
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public Response streamStaticContentPropFind(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose) {
		// we currently does not support PROPFIND method. This is added to prevent errors from rest easy api
		return Response.status(Status.METHOD_NOT_ALLOWED).header(HttpHeaders.ALLOW, GET_AND_HEAD).build();
	}

	/**
	 * Sends the HTTP headers for the requested static content. They are the same as the one send from the actual GET
	 * method.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional path parameter if not specified
	 *            default content purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @return A head response
	 */
	@HEAD
	@Path("static/" + CONTENT_ID_PARAM)
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public Response streamStaticContentHead(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose) {
		return downloadService
				.getContentHeadResponse(contentId, purpose,
						builder -> builder
								.header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // valid for one day
									.header("Access-Control-Allow-Origin", "*")
									.header("Access-Control-Allow-Methods", GET_AND_HEAD)
									.header("Access-Control-Allow-Headers", HttpHeaders.ACCEPT))
					.build();
	}

	/**
	 * Sends the content identified by the given content id and/or purpose to the caller. The method marks the content
	 * for cacheable and valid for a single day.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is optional path parameter if not specified
	 *            default content purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @param forDownload
	 *            if present the content will have Content-Disposition header to notify the browser to save the file
	 * @param fileName
	 *            if present the content will have Content-Disposition header to notify the browser to save the file
	 *            with the custom file name
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@Path("static/" + CONTENT_ID_PARAM)
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public void streamStaticContent(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose,
			@QueryParam("download") String forDownload, @QueryParam("fileName") String fileName,
			@Context HttpServletResponse response) {
		response.addHeader(HttpHeaders.CACHE_CONTROL, "max-age=86400"); // valid for one day
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", GET_AND_HEAD);
		response.addHeader("Access-Control-Allow-Headers", HttpHeaders.ACCEPT);
		downloadService.sendContent(contentId, purpose, Range.ALL, forDownload != null, response, fileName);
	}
	
	/**
	 * Stream the preview of the content identified by the given content id
	 * and/or purpose to the caller.
	 *
	 * @param contentId
	 *            the content id to fetch the content to
	 * @param purpose
	 *            the purpose of the content related to the instance. This is
	 *            optional path parameter if not specified default content
	 *            purpose will be used: {@link Content#PRIMARY_CONTENT}
	 * @param range
	 *            the HTTP header Range that specifies the range of the content
	 *            to return. The default value is all content
	 * @param response
	 *            the response to write the content to
	 */
	@GET
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	@Path(CONTENT_ID_PARAM + "/preview")
	public void streamPreview(@PathParam(CONTENT_ID) String contentId,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam(PURPOSE) String purpose,
			@DefaultValue(Range.DEFAULT_RANGE) @HeaderParam(Range.HEADER) Range range,
			@Context HttpServletResponse response) {
		downloadService.sendPreview(contentId, purpose, response, range);
	}
}
