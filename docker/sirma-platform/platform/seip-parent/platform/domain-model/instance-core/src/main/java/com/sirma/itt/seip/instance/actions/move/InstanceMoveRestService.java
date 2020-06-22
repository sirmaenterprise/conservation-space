package com.sirma.itt.seip.instance.actions.move;

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
 * Rest service that executes the move instance action.
 *
 * @author nvelkov
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceMoveRestService {
	@Inject
	private Actions actions;

	/**
	 * Execute the move action.
	 *
	 * @param request
	 *            {@link MoveActionRequest} containing the information for the move operation
	 * @return the moved instance
	 */
	@POST
	@Path("/{id}/actions/move")
	public Instance executeMoveAction(MoveActionRequest request) {
		return (Instance) actions.callAction(request);
	}
}
