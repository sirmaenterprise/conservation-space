package com.sirma.cmf.web.actionsmanager;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for evaluating instance actions.
 * 
 * @author svelikov
 */
@Path("/actions")
@Produces(MediaType.APPLICATION_JSON) 
@ApplicationScoped
public class ActionsProviderRestService extends EmfRestService {

	/** The actions provider. */
	@Inject
	private ActionsProvider actionsProvider;

	/** The time tracker. */
	private TimeTracker timeTracker;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		timeTracker = new TimeTracker();
	}

	/**
	 * Evaluate actions.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param contextId
	 *            the context id
	 * @param contextType
	 *            the context type
	 * @param placeholder
	 *            the placeholder
	 * @return the response
	 */
	@GET
	@Path("/")
	public Response evaluateActions(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType,
			@QueryParam("contextId") String contextId,
			@QueryParam("contextType") String contextType,
			@QueryParam("placeholder") String placeholder) {

		timeTracker.begin();
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)
				|| StringUtils.isNullOrEmpty(placeholder)) {
			log.warn(
					"Missing required arguments for actions evaluation instanceId[{}], instanceType[{}] or placeholder[{}]",
					instanceId, instanceType, placeholder);
			return buildBadRequestResponse("Missing required arguments for actions evaluation instanceId, instanceType or placeholder");
		}

		Instance instance = fetchInstance(instanceId, instanceType);
		Instance context = fetchInstance(contextId, contextType);
		List<Action> actionsByInstance = actionsProvider.evaluateActionsByInstance(instance,
				placeholder, context);
		JSONObject response = new JSONObject();
		JsonUtil.addActions(response, actionsByInstance);
		Response responseObject = buildResponse(Status.OK, response);

		log.debug(
				"Evaluating actions for instanceId[{}], instanceType[{}] and placeholder[{}] took {} sec",
				instanceId, instanceType, placeholder, timeTracker.stopInSeconds());
		return responseObject;
	}
}
