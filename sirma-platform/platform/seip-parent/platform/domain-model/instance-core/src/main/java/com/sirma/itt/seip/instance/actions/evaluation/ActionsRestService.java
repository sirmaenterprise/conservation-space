package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_CONTEXT_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PATH;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PLACEHOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.instance.actions.group.ActionMenu;

/**
 * Rest service for instance action evaluation. The actions are filtered for the instance and the user permissions over
 * it. The actions are collected from the state transitions definitions.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class ActionsRestService {

	@Inject
	private Actions actions;

	/**
	 * Evaluates actions for the given instance and returns collection without any group logic.
	 *
	 * @param id
	 *            the id of the target instance, which actions will be extracted
	 * @param contextId
	 *            the id of the context instance for the target
	 * @param placeholder
	 *            the placeholder of the action, used for actions filtering
	 * @param path
	 *            the path to the current instance. Used to resolve the context if it is not passed
	 * @return Json representation of filtered collection of {@link Action}.
	 */
	@GET
	@Path("/{id}/actions/flat")
	@SuppressWarnings("unchecked")
	public Set<Action> getFlatActions(@PathParam(KEY_ID) String id, @QueryParam(KEY_CONTEXT_ID) String contextId,
			@DefaultValue("") @QueryParam(KEY_PLACEHOLDER) String placeholder,
			@QueryParam(KEY_PATH) List<String> path) {
		ActionsListRequest request = new ActionsListRequest();
		request.setTargetId(id);
		request.setContextId(contextId);
		request.setPlaceholder(placeholder);
		request.setContextPath(new ArrayList<>(path));
		request.setFlatMenuType(true);
		return (Set<Action>) actions.callAction(request);
	}

	/**
	 * Evaluates actions for the given instance. Combines actions in menu structure based on properties values.
	 *
	 * @param id
	 *            the id of the target instance, which actions will be extracted
	 * @param contextId
	 *            the id of the context instance for the target
	 * @param placeholder
	 *            the placeholder of the action, used for actions filtering
	 * @param path
	 *            the path to the current instance. Used to resolve the context if it is not passed
	 * @return Json representation of filtered collection of {@link Action}.
	 */
	@GET
	@Path("/{id}/actions")
	public ActionMenu getActions(@PathParam(KEY_ID) String id, @QueryParam(KEY_CONTEXT_ID) String contextId,
			@DefaultValue("") @QueryParam(KEY_PLACEHOLDER) String placeholder,
			@QueryParam(KEY_PATH) List<String> path) {
		ActionsListRequest request = new ActionsListRequest();
		request.setTargetId(id);
		request.setContextId(contextId);
		request.setPlaceholder(placeholder);
		request.setContextPath(new ArrayList<>(path));
		request.setFlatMenuType(false);
		return (ActionMenu) actions.callAction(request);

	}
}
