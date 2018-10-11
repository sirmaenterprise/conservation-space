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
 * Handles requests for setting templates as primary.
 *
 * @author Viliar Tsonev
 */
@Path("instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
public class SetTemplateAsPrimaryRestService {

	@Inject
	private Actions actions;

	/**
	 * Sets the template as primary. If there is existing active primary template for the given rule, or with no rules
	 * it will be demoted.
	 * 
	 * @param request
	 *            is the {@link SetTemplateAsPrimaryActionRequest}
	 */
	@POST
	@Path("{id}/actions/set-template-as-primary")
	public void setAsPrimary(SetTemplateAsPrimaryActionRequest request) {
		actions.callAction(request);
	}
}
