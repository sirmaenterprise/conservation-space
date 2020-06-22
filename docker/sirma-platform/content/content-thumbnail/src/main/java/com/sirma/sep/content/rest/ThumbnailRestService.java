package com.sirma.sep.content.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.content.rendition.RenditionService;

/**
 * Provides methods for querying and manipulating instance thumbnails.
 *
 * @author yasko
 */
@Transactional
@Produces("image/png")
@Path(ThumbnailRestService.PATH)
public class ThumbnailRestService {

	public static final String PATH = "/thumbnails/{id}";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RenditionService renditionService;

	/**
	 * Retrieves the thumbnail for an instance.
	 *
	 * @param id
	 *            Instance identifier.
	 * @return Thumbnail image for the instance identified by the provided identifier or 404 if thumbnail is not found.
	 */
	@GET
	public Response load(@PathParam(KEY_ID) String id) {
		String thumbnail = renditionService.getThumbnail(id);

		if (StringUtils.isBlank(thumbnail)) {
			LOGGER.info("Could not find thumbnail for instance with id: {}", id);
			return Response.status(Status.NOT_FOUND).build();
		}

		// FIXME: CMF-21024 - when backend is refactored - remove this
		if (thumbnail.startsWith("data:image")) {
			// this should handle thumbnails of different types like image/png or image/jpg
			thumbnail = thumbnail.substring(thumbnail.indexOf(',') + 1);
		}
		return Response.ok(Base64.getDecoder().decode(thumbnail.getBytes(StandardCharsets.UTF_8))).build();
	}
}