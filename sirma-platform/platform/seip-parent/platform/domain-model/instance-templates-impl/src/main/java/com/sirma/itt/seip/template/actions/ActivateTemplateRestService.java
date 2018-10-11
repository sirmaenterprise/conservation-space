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
 * Handles template activation action requests.
 *
 * @author Viliar Tsonev
 */
@Path("instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
public class ActivateTemplateRestService {

	@Inject
	private Actions actions;

	/**
	 * Activates the template. When activated, the template becomes available for its forType, and it can be applied.
	 *
	 * @param request
	 *            is the request for template activation
	 */
	@POST
	@Path("{id}/actions/activate-template")
	public void activate(ActivateTemplateActionRequest request) {
		actions.callAction(request);
	}
}
