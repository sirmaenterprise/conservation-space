package com.sirma.itt.seip.template.actions;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Handles requests for editing template rules.
 *
 * @author Viliar Tsonev
 */
@Path("instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
public class EditTemplateRuleRestService {

	@Inject
	private Actions actions;

	/**
	 * Edits a rule of a template instance. If the rule's value is null or empty, the rule property is removed from the
	 * instance.
	 *
	 * @param request is the {@link EditTemplateRuleActionRequest} carrying the data
	 * @return the updated template instance
	 */
	@POST
	@Path("{id}/actions/edit-template-rule")
	public String editRule(EditTemplateRuleActionRequest request) {
		return Objects.toString(actions.callAction(request));
	}
}
