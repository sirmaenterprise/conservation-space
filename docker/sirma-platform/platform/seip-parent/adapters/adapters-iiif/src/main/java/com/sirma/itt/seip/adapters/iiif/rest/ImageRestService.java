package com.sirma.itt.seip.adapters.iiif.rest;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.iiif.Dimension;
import com.sirma.itt.seip.adapters.iiif.ImageManifestService;
import com.sirma.itt.seip.adapters.iiif.ImagePhysicalScaleService;
import com.sirma.itt.seip.adapters.iiif.Manifest;
import com.sirma.itt.seip.adapters.iiif.PhysicalScale;
import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * The image rest service, which provides methods for creation,access and update of image manifests.
 *
 * @author Nikolay Ch
 * @author radoslav
 */
@Transactional
@Path("/image")
@ApplicationScoped
public class ImageRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageRestService.class);

	@Inject
	private ImageManifestService manifestService;

	@Inject
	private ImagePhysicalScaleService physicalScaleService;

	/**
	 * Returns the physical scale ratio of image
	 *
	 * @param imageId
	 *            The image id
	 * @param width
	 *            The width of the canvas in which the image is visualized
	 * @param height
	 *            The height of the canvas in which the image is visualized
	 * @return Not found if scale cannot be calculated
	 */
	@GET
	@PublicResource(tenantParameterName = "tenant")
	@Path("/{tenant}/{id}/physicalScale")
	public Response getPhysicalScale(@PathParam("id") String imageId, @QueryParam("width") Integer width,
			@QueryParam("height") Integer height) {

		PhysicalScale scale = physicalScaleService.getPhysicalScale(imageId, new Dimension<>(width, height));

		if (scale != null) {
			return Response.ok(scale.getAsInputStream()).build();
		}

		return Response.status(Status.NOT_FOUND).build();

	}

	/**
	 * Returns the manifest with the given id if it exists.
	 *
	 * @param manifestId
	 *            the id of the manifest
	 * @param response
	 *            the response with the manifest
	 * @return server error if the id is empty, not found if there is not a manifest with that id
	 */
	@GET
	@Path("/manifest/{manifestId}")
	public Response getManifest(@PathParam("manifestId") String manifestId, @Context HttpServletResponse response) {
		return processResponse(manifestService.getManifest(manifestId), response);
	}

	/**
	 * Creates the manifest.
	 *
	 * @param data
	 *            the json object with the image widget id and the selected image ids.
	 * @return the id of the created manifest
	 */
	@POST
	@Path("/manifest/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createManifest(JsonObject data) {
		return Response.ok(processProvidedData(null, data), MediaType.TEXT_PLAIN).build();
	}

	/**
	 * Updates the manifest.
	 *
	 * @param data
	 *            the json object with the id of the manifest which will be updated, the image widget id and the ids of
	 *            the selected images
	 * @return the id of the updated manifest
	 */
	@POST
	@Path("/manifest/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateManifest(JsonObject data) {
		String manifestId = data.getString("manifestId");
		return Response.ok(processProvidedData(manifestId, data), MediaType.TEXT_PLAIN).build();
	}

	/**
	 * Extracts the image widget id and the selected image ids and calls the manifest service which will create or
	 * update the manifest.
	 *
	 * @param manifestId
	 *            the id of the manifest which will be updated or null if a manifest must be created
	 * @param data
	 *            the json object provided by the client
	 * @return the id of the updated/created manifest
	 */
	private String processProvidedData(String manifestId, JsonObject data) {
		String imageWidgetId = data.getString("imageWidgetId");
		List<? extends Serializable> selectedImageIds = JSON.getStringArray(data, "selectedImageIds");
		return manifestService.processManifest(manifestId, imageWidgetId, selectedImageIds);
	}

	/**
	 * Checks if the manifest exists, adds the headers and sends the response.
	 *
	 * @param manifest
	 *            the object with the information of the manifest
	 * @param response
	 *            the response
	 * @return the updated response
	 */
	private static Response processResponse(Manifest manifest, HttpServletResponse response) {
		if (!manifest.exists()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		sendContent(manifest, response);
		return Response.ok().build();
	}

	/**
	 * Adds the necessary headers and sends the response.
	 *
	 * @param manifest
	 *            the manifest information
	 * @param response
	 *            the response
	 */
	private static void sendContent(Manifest manifest, HttpServletResponse response) {
		addHeaders(manifest, response);
		try (BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
				InputStream in = manifest.getInputStream()) {
			IOUtils.copyLarge(in, out);
			out.flush();
		} catch (Exception e) {
			LOGGER.trace("", e);
			LOGGER.warn("Client disconnected during manifest send: {}", e.getMessage());
		}
	}

	/**
	 * Adds the necessary headers to the response.
	 *
	 * @param manifest
	 *            the object that contains the manifest information
	 * @param response
	 *            the response
	 */
	private static void addHeaders(Manifest manifest, HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(MediaType.APPLICATION_JSON);
		long contentLength = manifest.getLength();
		response.setContentLengthLong(contentLength);
		response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");
	}
}
