package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PLACEHOLDER;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.instance.actions.group.ActionMenu;

/**
 * Rest service for instance action evaluation. The actions are filtered for the instance and the user permissions over
 * it. The actions are collected from the state transitions definitions.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class ActionsRestService {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceActionsEvaluatior instanceActionsEvaluatior;

	/**
	 * Evaluates actions for the given instance and returns collection without applying any grouping logic.
	 *
	 * @param id of the target instance, which actions will be extracted
	 * @param contextId the id of the context instance for the target
	 * @param placeholder of the actions, used for filtering
	 * @param path to the current instance. Used to resolve the context, if it is not passed
	 * @return JOSN representation of filtered collection of {@link Action}s - flat structure
	 */
	@GET
	@Path("/{id}/actions/flat")
	public Set<Action> getFlatActions(@PathParam(KEY_ID) String id,
			@DefaultValue("") @QueryParam(KEY_PLACEHOLDER) String placeholder) {
		return instanceActionsEvaluatior.evaluate(toRequest(id, placeholder));
	}

	private InstanceActionsRequest toRequest(String id, String placeholder) {
		return new InstanceActionsRequest(domainInstanceService.loadInstance(id)).setPlaceholder(placeholder);
	}

	/**
	 * Evaluates actions for the given instance. Combines actions in menu structure based on properties values.
	 *
	 * @param id of the target instance, which actions will be extracted
	 * @param placeholder of the actions, used for filtering
	 * @return JOSN representation of filtered collection of {@link Action}s. It represents complex structure of menu
	 *         with unlimited number of sub menus, which also can contain sub menus
	 */
	@GET
	@Path("/{id}/actions")
	public ActionMenu getActions(@PathParam(KEY_ID) String id,
			@DefaultValue("") @QueryParam(KEY_PLACEHOLDER) String placeholder) {
		return instanceActionsEvaluatior.evaluateAndBuildMenu(toRequest(id, placeholder));
	}
}