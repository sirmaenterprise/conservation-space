package com.sirma.itt.seip.instance.actions.transition;

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
 * Rest service for execution on operations that changes instance states, like Approve, Reject, etc.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceTransitionRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes operations that change the instance state. The actual operation that will be executed is the
	 * userOperation defined in the request.
	 *
	 * @param request
	 *            {@link TransitionActionRequest} containing the information for the operation, like - user operation,
	 *            context path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@POST
	@Path("/{id}/actions/transition")
	public Instance executeChangeStateAction(TransitionActionRequest request) {
		return (Instance) actions.callAction(request);
	}

}
