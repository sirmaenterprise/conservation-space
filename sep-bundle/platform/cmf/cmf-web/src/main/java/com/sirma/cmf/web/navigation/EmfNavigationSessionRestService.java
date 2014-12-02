package com.sirma.cmf.web.navigation;

import java.util.Stack;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * EmfNavigationSessionRestService provides access and management functions for EmfSessionHandler.
 * 
 * @author svelikov
 */
@Path("/navigation")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class EmfNavigationSessionRestService extends EmfRestService {

	/** The emf navigation session. */
	@Inject
	private EmfNavigationSession emfNavigationSession;

	/**
	 * Clear history with GET http method.
	 * 
	 * @return the response
	 */
	@GET
	@Path("/delete")
	public Response clearHistory() {
		clearNavigationHistory();
		return buildResponse(Status.OK, null);
	}

	/**
	 * Clear history with DELETE http method.
	 * 
	 * @return the response
	 */
	@DELETE
	@Path("/")
	public Response clearHistory2() {
		clearNavigationHistory();
		return buildResponse(Status.OK, null);
	}

	/**
	 * Gets the history.
	 * 
	 * @return the history
	 */
	@GET
	@Path("/")
	public Response getHistory() {
		JSONObject response = new JSONObject();
		JSONArray navigationHistoryJson = convertHistoryToJson(emfNavigationSession
				.getTrailPointStack());
		JsonUtil.addToJson(response, "history", navigationHistoryJson);
		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * Convert history to json.
	 * 
	 * @param stack
	 *            the stack
	 * @return the jSON array
	 */
	private JSONArray convertHistoryToJson(Stack<NavigationPoint> stack) {
		JSONArray array = new JSONArray();
		for (NavigationPoint point : stack) {
			JSONObject pointObject = new JSONObject();
			JsonUtil.addToJson(pointObject, "viewId", point.getViewId());
			JsonUtil.addToJson(pointObject, "actionMethod", point.getActionMethod());
			JsonUtil.addToJson(pointObject, "outcome", point.getOutcome());
			JsonUtil.addToJson(pointObject, "instanceId", point.getInstanceId());
			array.put(pointObject);
		}
		return array;
	}

	/**
	 * Clear navigation history.
	 */
	private void clearNavigationHistory() {
		emfNavigationSession.getTrailPointStack().clear();
		emfNavigationSession.pushHomePagePoint();
		log.debug("Cleared navigation history!");
	}

}
