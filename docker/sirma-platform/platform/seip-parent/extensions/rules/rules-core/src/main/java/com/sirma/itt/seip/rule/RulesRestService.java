package com.sirma.itt.seip.rule;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleState;
import com.sirma.itt.emf.rule.invoker.RuleExecutionStatusAccessor;
import com.sirma.itt.emf.rule.invoker.RuleInvoker;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;

/**
 * Service to provide status information about the running rules over instances
 *
 * @author BBonev
 */
@Singleton
@Path("/rules")
@Produces(MediaType.APPLICATION_JSON)
public class RulesRestService {

	@Inject
	private RuleInvoker ruleInvoker;
	@Inject
	private RuleStore ruleStore;

	/**
	 * Gets the all running tasks.
	 *
	 * @return the all running tasks
	 */
	@GET
	@Path("running")
	public Response getAllRunningTasks() {
		Map<Serializable, Collection<RuleExecutionStatusAccessor>> activeRules = ruleInvoker.getAllActiveRules();
		JSONArray result = new JSONArray();
		for (Entry<Serializable, Collection<RuleExecutionStatusAccessor>> entry : activeRules.entrySet()) {
			JSONObject instanceRules = buildInstanceRuleData(entry.getKey(), entry.getValue());
			result.put(instanceRules);
		}
		return RestUtil.buildDataResponse(result);
	}

	/**
	 * Gets the instance rules.
	 *
	 * @param id
	 *            the id
	 * @return the instance rules
	 */
	@GET
	@Path("running/{id}")
	public Response getInstanceRules(@PathParam("id") String id) {
		Collection<RuleExecutionStatusAccessor> activeRules = ruleInvoker.getActiveRules(id);
		JSONObject ruleData = buildInstanceRuleData(id, activeRules);
		JSONArray result = new JSONArray();
		result.put(ruleData);
		return RestUtil.buildDataResponse(result);
	}

	/**
	 * Cancel instance rules.
	 *
	 * @param id
	 *            the id
	 * @return the state before cancellation
	 */
	@DELETE
	@Path("running/{id}")
	public Response cancelInstanceRules(@PathParam("id") String id) {
		// return the state before cancellation
		Response response = getInstanceRules(id);
		ruleInvoker.cancelRunningRulesForInstance(id);
		return response;
	}

	/**
	 * Cancel instance rules.
	 *
	 * @param id
	 *            the id
	 * @return the state before cancellation
	 */
	@GET
	@Path("running/{id}/cancel")
	public Response cancelInstanceRulesWithGet(@PathParam("id") String id) {
		// return the state before cancellation
		Response response = getInstanceRules(id);
		ruleInvoker.cancelRunningRulesForInstance(id);
		return response;
	}

	private static JSONObject buildInstanceRuleData(Serializable key, Collection<RuleExecutionStatusAccessor> value) {
		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, "id", key);
		JSONArray rules = new JSONArray();
		for (RuleExecutionStatusAccessor statusAccessor : value) {
			JSONObject ruleData = new JSONObject();
			long executionTime = statusAccessor.executionTime();
			JsonUtil.addToJson(ruleData, "currentRule", statusAccessor.getCurrentlyProcessedRule());
			JsonUtil.addToJson(ruleData, "operation", statusAccessor.getTriggerOperation());
			long currentRuleExecutionTime = statusAccessor.currentRuleExecutionTime();
			JsonUtil.addToJson(ruleData, "currentRuleActiveTime", currentRuleExecutionTime);
			JsonUtil.addToJson(ruleData, "currentRuleActiveTimeInSeconds",
					TimeUnit.MILLISECONDS.toSeconds(currentRuleExecutionTime));
			JsonUtil.addToJson(ruleData, "runTime", executionTime);
			JsonUtil.addToJson(ruleData, "runTimeInSeconds", TimeUnit.MILLISECONDS.toSeconds(executionTime));
			JsonUtil.addToJson(ruleData, "isWaiting", executionTime < 0);
			JsonUtil.addToJson(ruleData, "isDone", statusAccessor.isDone());
			JsonUtil.addToJson(ruleData, "pending", new JSONArray(statusAccessor.getPendingRules()));
			JsonUtil.addToJson(ruleData, "failed", new JSONArray(statusAccessor.getFailedRules()));
			JsonUtil.addToJson(ruleData, "completed", new JSONArray(statusAccessor.getCompletedRules()));
			RuleState currentState = statusAccessor.getCurrentState();
			if (currentState != null) {
				JsonUtil.addToJson(ruleData, "ruleStatus", currentState.toJSONObject());
			}
			rules.put(ruleData);
		}
		JsonUtil.addToJson(data, "rules", rules);
		return data;
	}

	/**
	 * List all rules.
	 *
	 * @return the response
	 */
	@GET
	public Response listAllRules() {
		Stream<JSONObject> activeRules = ruleStore.listActiveRules().map(rule -> convertRuleToJson(rule, Boolean.TRUE));
		Stream<JSONObject> inactiveRules = ruleStore
				.listInactiveRules()
					.map(rule -> convertRuleToJson(rule, Boolean.FALSE));

		List<JSONObject> allRules = Stream
				.concat(activeRules, inactiveRules)
					.collect(Collectors.toCollection(LinkedList::new));
		return RestUtil.buildDataResponse(allRules);
	}

	/**
	 * List active rules.
	 *
	 * @return the response
	 */
	@GET
	@Path("active")
	public Response listActiveRules() {
		List<JSONObject> rules = ruleStore.listActiveRules().map(rule -> convertRuleToJson(rule, Boolean.TRUE)).collect(
				Collectors.toCollection(LinkedList::new));
		return RestUtil.buildDataResponse(rules);
	}

	/**
	 * List inactive rules.
	 *
	 * @return the response
	 */
	@GET
	@Path("inactive")
	public Response listInactiveRules() {
		List<JSONObject> rules = ruleStore
				.listInactiveRules()
					.map(rule -> convertRuleToJson(rule, Boolean.FALSE))
					.collect(Collectors.toCollection(LinkedList::new));
		return RestUtil.buildDataResponse(rules);
	}

	private static JSONObject convertRuleToJson(InstanceRule rule, Boolean isActive) {
		JSONObject object = new JSONObject();

		JsonUtil.addToJson(object, "id", rule.getRuleInstanceName());
		JsonUtil.addToJson(object, "active", isActive);
		JsonUtil.addToJson(object, "async", rule.isAsyncSupported());
		JsonUtil.addToJson(object, "operations", new JSONArray(rule.getSupportedOperations()));
		JsonUtil.addToJson(object, "definitions", new JSONArray(rule.getSupportedDefinitions()));
		JsonUtil.addToJson(object, "types", new JSONArray(rule.getSupportedObjects()));

		return object;
	}

	/**
	 * Activate rules.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("active")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response activateRules(String data) {

		JSONArray toActivate = RestUtil.readDataRequest(data);
		for (int i = 0; i < toActivate.length(); i++) {
			String ruleId = JsonUtil.getStringFromArray(toActivate, i);
			ruleStore.activateRule(ruleId);
		}

		return Response.ok().build();
	}

	/**
	 * Deactivate rules.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("inactive")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deactivateRules(String data) {

		JSONArray toActivate = RestUtil.readDataRequest(data);
		for (int i = 0; i < toActivate.length(); i++) {
			String ruleId = JsonUtil.getStringFromArray(toActivate, i);
			ruleStore.deactivateRule(ruleId);
		}

		return Response.ok().build();
	}
}
