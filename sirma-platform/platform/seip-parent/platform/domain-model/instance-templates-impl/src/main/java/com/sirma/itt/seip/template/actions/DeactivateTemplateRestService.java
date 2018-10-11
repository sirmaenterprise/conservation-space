package com.sirma.itt.seip.template.actions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Handles template deactivation action requests.
 *
 * @author Viliar Tsonev
 */
@Path("instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
public class DeactivateTemplateRestService {

	@Inject
	private Actions actions;

	/**
	 * Deactivates the template. Its status becomes Inactive and it is not available to be applied to its group anymore.
	 *
	 * @param request
	 *            is the request for template deactivation
	 */
	@POST
	@Path("{id}/actions/deactivate-template")
	public void deactivate(DeactivateTemplateActionRequest request) {
		actions.callAction(request);
	}
}
