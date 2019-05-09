package com.sirma.itt.seip.content.processing;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Rest endpoint for content migration
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@ApplicationScoped
@Path("/migration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContentMigrationRest {

	@Inject
	private ContentMigrator downloader;

	/**
	 * Trigger migration of the all idocs to download all external images. The images will be downloaded only if they match the given address.
	 *
	 * @param allowedAddresses accepts a json array containing list of allowed addresses.
	 * @return the number of files that will be processed
	 */
	@POST
	@Path("/downloadEmbeddedImages")
	@Transactional
	public Response downloadEmbeddedImages(List<String> allowedAddresses) {
		if (allowedAddresses.isEmpty()) {
			throw new BadRequestException("List of allowed addresses is mandatory");
		}
		int filesToBeProcessed = downloader.downloadEmbeddedImages(buildRegexFilters(allowedAddresses));
		String response = Json.createObjectBuilder().add("files", filesToBeProcessed).build().toString();
		return Response.accepted(response).build();
	}

	private String[] buildRegexFilters(List<String> patterns) {
		return patterns.stream().map(filter -> filter.replace(".", "\\.").replace("*", ".*")).toArray(String[]::new);
	}
}
