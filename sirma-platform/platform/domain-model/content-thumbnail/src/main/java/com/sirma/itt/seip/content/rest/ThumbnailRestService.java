package com.sirma.itt.seip.content.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.content.rendition.RenditionService;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;

/**
 * Provides methods for querying and manipulating instance thumbnails.
 *
 * @author yasko
 */
@Produces("image/png")
@Path(ThumbnailRestService.PATH)
public class ThumbnailRestService {
	public static final String PATH = "/thumbnails/{id}";

	@Inject
	private RenditionService renditionService;

	/**
	 * Retrieves the thumbnail for an instance.
	 *
	 * @param id
	 *            Instance identifier.
	 * @return Thumbnail image for the instance identified by the provided
	 *         identifier.
	 */
	@GET
	public byte[] load(@PathParam(KEY_ID) String id) {
		String thumbnail = renditionService.getThumbnail(id);

		if (StringUtils.isBlank(thumbnail)) {
			throw new ResourceNotFoundException(id);
		}

		// FIXME: CMF-21024 - when backend is refactored - remove this
		if (thumbnail.startsWith("data:image")) {
			// this should handle thumbnails of different types like image/png or image/jpg
			thumbnail = thumbnail.substring(thumbnail.indexOf(',') + 1);
		}

		return Base64.getDecoder().decode(thumbnail.getBytes(StandardCharsets.UTF_8));
	}
}
