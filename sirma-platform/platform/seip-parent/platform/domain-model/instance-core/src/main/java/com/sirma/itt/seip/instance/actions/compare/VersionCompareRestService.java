package com.sirma.itt.seip.instance.actions.compare;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Provides service for comparing preview contents of two versions of one and the same instance.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class VersionCompareRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes operation that compares the primary content of two versions.
	 *
	 * @param request
	 *            contains the information required for operation execution
	 * @return link with which the result file from the compare could be download
	 */
	@POST
	@Path("/{id}/actions/compare-versions")
	public String compareVersions(VersionCompareRequest request) {
		return (String) actions.callAction(request);
	}

}
