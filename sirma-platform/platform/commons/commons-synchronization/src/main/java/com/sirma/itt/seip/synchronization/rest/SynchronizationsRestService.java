package com.sirma.itt.seip.synchronization.rest;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationResultState;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;

/**
 * Rest service for managing available synchronizations and triggering them.
 *
 * @author BBonev
 */
@ApplicationScoped
@Path("/synchronizations")
@Produces(MediaType.APPLICATION_JSON)
public class SynchronizationsRestService {

	@Inject
	private SynchronizationRunner synchronizationRunner;

	/**
	 * Gets the available synchronizations.
	 *
	 * @return the available synchronizations
	 */
	@GET
	public String getAvailableSynchronizations() {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		Collection<String> available = synchronizationRunner.getAvailable();
		for (String string : available) {
			builder.add(string);
		}
		return builder.build().toString();
	}

	/**
	 * Run a concrete synchronization. <br>
	 * Example configuration:
	 *
	 * <pre>
	 * <code>{
	 *   "force": true
	 * }</code>
	 * </pre>
	 *
	 * @param id
	 *            the id of the synchronization to run
	 * @param syncRuntimeConfiguration
	 *            the runtime synchronization configuration to use during this synchronization session
	 * @return the result of the synchronization
	 */
	@POST
	@Path("{id}")
	public String runSynchronization(@PathParam(RequestParams.KEY_ID) String id,
			SyncRuntimeConfiguration syncRuntimeConfiguration) {
		return toJson(synchronizationRunner.runSynchronization(id, syncRuntimeConfiguration)).build().toString();
	}

	/**
	 * Run all available synchronizations in the order they are registered. <br>
	 * Example configuration:
	 *
	 * <pre>
	 * <code>{
	 *   "force": true
	 * }</code>
	 * </pre>
	 *
	 * @param syncRuntimeConfiguration
	 *            the runtime synchronization configuration to use during this synchronization session
	 * @return the result of all run synchronizations
	 */
	@POST
	@Path("/runAll")
	public String runAll(SyncRuntimeConfiguration syncRuntimeConfiguration) {
		Collection<SynchronizationResultState> result = synchronizationRunner.runAll(syncRuntimeConfiguration);
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (SynchronizationResultState state : result) {
			arrayBuilder.add(toJson(state));
		}
		return arrayBuilder.build().toString();
	}

	private static JsonObjectBuilder toJson(SynchronizationResultState state) {
		JsonObjectBuilder builder = Json.createObjectBuilder();

		builder.add("name", state.getName());
		if (state.getException() != null) {
			builder.add("exception", state.getException().getMessage());
		} else {
			builder.add("added", toArray(state.getAdded()));
			builder.add("removed", toArray(state.getRemoved()));
			builder.add("modified", toArray(state.getModified()));
			builder.add("duration", state.getDuration());
		}
		return builder;
	}

	private static JsonArrayBuilder toArray(Collection<Object> collection) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		collection.forEach(e -> builder.add(e.toString()));
		return builder;
	}
}
