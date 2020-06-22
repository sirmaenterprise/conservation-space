package com.sirma.itt.seip.instance.actions.compare;

import static com.sirma.itt.seip.instance.actions.compare.VersionCompareRequest.COMPARE_VERSIONS;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.annotations.Cache;

/**
 * Provides services for comparing versions of an instance.
 *
 * @author A. Kunchev
 * @author yasko
 */
@Path("/instances")
@Produces("application/pdf")
@ApplicationScoped
public class VersionCompareRestService {

	@Inject
	private Actions actions;

	@Context
	private HttpHeaders headers;

	/**
	 * Compares two versions of an instance.
	 *
	 * @param id
	 *            Instance identifier.
	 * @param auth
	 *            Authorization header value.
	 * @param first
	 *            First version identifier.
	 * @param second
	 *            Second version identifier.
	 * @return File containing the diff between the provided instance versions.
	 */
	@GET
	@Cache(VersionCompareCacheHandler.class)
	@Path("/{id}/actions/compare-versions")
	public File compareVersions(@NotNull @PathParam(KEY_ID) String id, @HeaderParam(AUTHORIZATION) String auth,
			@NotNull @QueryParam("first") String first, @NotNull @QueryParam("second") String second) {

		VersionCompareRequest compareRequest = new VersionCompareRequest();
		compareRequest.setTargetId(id);
		compareRequest.setUserOperation(COMPARE_VERSIONS);
		compareRequest.setFirstSourceId(first);
		compareRequest.setSecondSourceId(second);
		compareRequest.setAuthentication(auth.split(" ")[1]);

		return (File) actions.callAction(compareRequest);
	}
}
