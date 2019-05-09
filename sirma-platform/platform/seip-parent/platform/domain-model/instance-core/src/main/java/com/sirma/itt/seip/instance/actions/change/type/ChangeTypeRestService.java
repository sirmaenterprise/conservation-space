package com.sirma.itt.seip.instance.actions.change.type;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service that expose instance operation {@code changeType}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/02/2019
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class ChangeTypeRestService {
	@Inject
	private Actions actions;

	@Inject
	private InstanceTypeMigrationCoordinator typeMigrationCoordinator;

	/**
	 * Executes clone on an {@link Instance} object. This method only clones a given {@link Instance} object without
	 * persisting it anywhere.
	 *
	 * @param id of the instance
	 * @return the newly cloned instance
	 */
	@GET
	@Path("/{id}/actions/changeType")
	public Instance getInstanceAsType(@PathParam(KEY_ID) String id, @QueryParam("asType") String asType) {
		return typeMigrationCoordinator.getInstanceAs(id, asType);
	}

	/**
	 * Executes the clone operation. This is the service that persists an Instance with it's corresponding content.
	 *
	 * @param request contains the request data
	 * @return the cloned instance
	 */
	@POST
	@Path("/{id}/actions/changeType")
	public Instance executeCloneAction(ChangeTypeRequest request) {
		return (Instance) actions.callAction(request);
	}
}
