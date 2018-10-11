package com.sirma.itt.seip.instance.actions.revert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest end point for executing revert on version instances.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class RevertVersionRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes revert operation on given version instance. The current instance in which will be revert will be locked
	 * while the process is executed.
	 *
	 * @param request
	 *            object that contains the request data for the operation execution
	 * @return the reverted instance
	 * @see RevertVersionAction
	 */
	@POST
	@Path("/{id}/actions/revert-version")
	public Instance revertVersion(RevertVersionRequest request) {
		return (Instance) actions.callAction(request);
	}

}
