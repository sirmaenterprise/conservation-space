package com.sirma.itt.seip.permissions.sync.rest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.permissions.sync.NoSynchronizationException;
import com.sirma.itt.seip.permissions.sync.PermissionSynchronizationService;
import com.sirma.itt.seip.permissions.sync.PermissionSynchronizationService.SyncExecution;
import com.sirma.itt.seip.permissions.sync.SynchronizationAlreadyRunningException;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;

/**
 * Rest service for permission synchronization execution
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/06/2017
 */
@ApplicationScoped
@Path("/permissions/sync")
@Produces(MediaType.APPLICATION_JSON)
@AdminResource
public class PermissionSynchronizationRestService {

	private static final String MESSAGE = "message";
	private static final String URI = "uri";

	@Inject
	private PermissionSynchronizationService synchronizationService;

	/**
	 * Trigger initial synchronization of the two phase process over all instances
	 *
	 * @return the batch job execution identifier that processes the current request.
	 * @throws SynchronizationAlreadyRunningException if other synchronization is already running
	 */
	@GET
	public Response syncAllDryRun() throws SynchronizationAlreadyRunningException {
		long jobExecutionId = synchronizationService.triggerPermissionChecking().getExecutionId();
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("execution-id", jobExecutionId);
		builder.add(MESSAGE, "Started permission synchronization check. Need confirmation before actual "
				+ "synchronization.");

		JsonObjectBuilder options = Json.createObjectBuilder();
		options.add("confirm",
				buildOption("To confirm synchronization and write changes", "/permissions/sync/confirm"));
		options.add("info", Json.createObjectBuilder().add(MESSAGE, "Get current data status and info about the "
				+ "found changes").add(URI, "/permissions/sync/info"));
		options.add("wait", Json.createObjectBuilder().add(MESSAGE, "Wait for all data to become available").add
				(URI, "/permissions/sync/wait"));
		options.add("cancel", Json.createObjectBuilder().add(MESSAGE, "Cancel synchronization at any moment")
				.add(URI, "/permissions/sync/cancel"));
		builder.add("options", options);
		return Response.ok(builder.build().toString()).build();
	}

	private static JsonObjectBuilder buildOption(String message, String uri) {
		return Json.createObjectBuilder().add(MESSAGE, message).add(URI, uri);
	}

	/**
	 * Get information about the running synchronization
	 *
	 * @return list of currently processed and affected instances
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	@GET
	@Path("info")
	public Response getData() throws NoSynchronizationException {
		SyncExecution syncExecution = synchronizationService.getCurrentExecution();

		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("isDone", syncExecution.isDone());
		builder.add("isCancelled", syncExecution.isCancelled());

		JsonArrayBuilder changes = Json.createArrayBuilder();
		syncExecution.getData().forEach(changes::add);

		builder.add("changes", changes);
		return Response.ok(builder.build().toString()).build();
	}

	/**
	 * Blocks and waits for the synchronization to finish and returns all affected instance identifiers
	 *
	 * @return affected instance ids
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	@GET
	@Path("wait")
	public Response waitForData() throws NoSynchronizationException {
		Optional<List<String>> data = synchronizationService.getCurrentExecution().waitForData();
		return Response.ok(data.orElse(Collections.emptyList())).build();
	}

	/**
	 * Cancel any running two phase synchronization
	 *
	 * @return success message
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	@GET
	@Path("cancel")
	public Response cancelProcessing() throws NoSynchronizationException {
		synchronizationService.cancelSynchronization();
		return RestUtil.buildDataResponse("Job canceled", MESSAGE);
	}

	/**
	 * Triggers the second phase of the synchronization that will apply the changes to the database. If the first
	 * phase is not completed, the method will block and wait for the data to get available
	 *
	 * @return the batch job execution identifier that processes the second phase of the synchronization
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	@GET
	@Path("confirm")
	public Response confirmExecution() throws NoSynchronizationException {
		long jobExecutionId = synchronizationService.applySynchronizationChanges();
		if (jobExecutionId < 0) {
			return RestUtil.buildDataResponse("No changes found", MESSAGE);
		}
		return buildExecutionIdResponse(jobExecutionId, "Committing changes for synchronization");
	}

	/**
	 * Perform synchronization for the given list of instance identifiers.
	 *
	 * @param ids the identifiers to process
	 * @return the batch job execution identifier that processes the current request
	 */
	@POST
	@Path("custom")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response syncGivenInstances(List<String> ids) {
		long jobExecutionId = synchronizationService.syncGivenInstances(ids);
		return buildExecutionIdResponse(jobExecutionId, "Trigger synchronization for the given instance identifiers");
	}

	private static Response buildExecutionIdResponse(long jobExecutionId, String message) {
		return Response.ok(Json.createObjectBuilder()
				.add("execution-id", jobExecutionId)
				.add(MESSAGE, message)
				.build().toString()).build();
	}
}
