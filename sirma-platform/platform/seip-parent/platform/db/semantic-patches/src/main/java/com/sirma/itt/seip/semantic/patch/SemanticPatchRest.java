package com.sirma.itt.seip.semantic.patch;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * The SemanticPatchRest executes patches and interacts with the semantic patching subsystem
 *
 * @author bbanchev
 */
@Transactional
@Path("/patch/semantic")
@ApplicationScoped
public class SemanticPatchRest {
	private static final MediaType APPLICATION_ZIP = new MediaType("application", "x-zip-compressed");
	@Inject
	private BackingPatchService patchUtilService;
	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Execute patch using provided files.
	 *
	 * @param form
	 *            the multipart form
	 * @return the response - ok or the error
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response patch(MultipartFormDataInput form) {
		try {
			// get the base json request
			Map<String, List<InputPart>> parts = form.getFormDataMap();
			String tenantId = securityContextManager.getCurrentContext().getCurrentTenantId();
			for (Entry<String, List<InputPart>> entry : parts.entrySet()) {
				if (entry.getValue().size() != 1) {
					return Response.status(Status.NOT_IMPLEMENTED).type(MediaType.TEXT_PLAIN)
							.entity("Currently supported model patch is single zip file!").build();
				}

				InputPart inputPart = entry.getValue().get(0);
				String name = getFileName(entry.getKey(), inputPart.getHeaders());
				MediaType mediaType = inputPart.getMediaType();
				if (MediaType.APPLICATION_OCTET_STREAM_TYPE.isCompatible(mediaType)
						|| APPLICATION_ZIP.equals(mediaType)) {
					patchUtilService.runPatchAndBackup(inputPart.getBody(InputStream.class, Class.class), name,
							tenantId);
				}
			}

		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(e).build();
		}
		return Response.ok().build();
	}

	/**
	 * http://www.mkyong.com/webservices/jax-rs/file-upload-example-in-resteasy/ Header sample {
	 * Content-Type=[image/png], Content-Disposition=[form-data; name="file"; filename="filename.extension"] }
	 **/
	private static String getFileName(String entryId, MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			String trimmed = filename.trim();
			if (trimmed.startsWith("filename")) {
				String[] name = trimmed.split("=");
				return name[1].trim().replaceAll("\"", "");
			}
		}
		return entryId + ".zip";
	}
}
