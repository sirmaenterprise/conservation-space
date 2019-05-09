package com.sirma.itt.seip.instance.actions.clone;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Contains the web services that are needed for the clone operation.
 * <p>
 * The web uses several services for cloning the instance
 * <li>http GET that clones the {@link Instance} object, but without saving it
 * <li>http post to do the actual persist of the cloned instance
 * </p>
 *
 * @author Ivo Rusev on 9.12.2016
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceCloneRestService {

	@Inject
	private Actions actions;

	@Inject
	private DomainInstanceService domainInstanceService;

	/**
	 * Executes the clone operation. This is the service that persists an Instance with it's corresponding content.
	 *
	 * @param request contains the request data
	 * @return the cloned instance
	 */
	@POST
	@Path("/{id}/actions/clone")
	public Instance executeCloneAction(InstanceCloneRequest request) {
		return (Instance) actions.callAction(request);
	}

	/**
	 * Executes clone on an {@link Instance} object. This method only clones a given {@link Instance} object without
	 * persisting it anywhere.
	 *
	 * @param id of the instance
	 * @return the newly cloned instance
	 */
	@GET
	@Path("/{id}/actions/clone")
	public Instance clone(@PathParam(KEY_ID) String id) {
		return domainInstanceService.clone(id, new Operation(InstanceCloneRequest.OPERATION_NAME, true));
	}
}
