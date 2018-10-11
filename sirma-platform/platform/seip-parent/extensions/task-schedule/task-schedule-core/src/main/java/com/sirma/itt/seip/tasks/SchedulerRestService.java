package com.sirma.itt.seip.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;

/**
 * Rest service to provide access {@link SchedulerService}
 *
 * @author BBonev
 */
@Transactional
@Path("/scheduleTasks")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SchedulerRestService {
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private DbDao dbDao;
	@Inject
	private TypeConverter typeConverter;

	@Inject
	private TimedScheduleTrigger timedExecutor;
	@Inject
	private CronScheduleTrigger cronTrigger;

	/**
	 * List all scheduled task. The method could filter by state: active or all of them
	 *
	 * @param onlyActive
	 *            if should return only active tasks
	 * @return the response
	 */
	@GET
	public Response listAll(@DefaultValue("true") @QueryParam("active") Boolean onlyActive) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		Collection<SchedulerEntryStatus> states = SchedulerEntryStatus.ACTIVE_STATES;
		if (Boolean.FALSE.equals(onlyActive)) {
			states = SchedulerEntryStatus.ALL_STATES;
		}
		args.add(new Pair<String, Object>("status", states));
		List<Long> list = dbDao.fetchWithNamed(SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY, args);
		List<SchedulerEntry> entries = schedulerService.loadByDbId(list);
		return RestUtil.buildDataResponse(new JSONArray(typeConverter.convert(JSONObject.class, entries)));
	}

	/**
	 * List active async tasks.
	 *
	 * @return the response
	 */
	@GET
	@Path("async")
	public Response listActiveAsyncTasks() {
		Collection<JSONObject> active = typeConverter.convert(JSONObject.class, timedExecutor.getActiveTasks());
		Collection<JSONObject> cron = typeConverter.convert(JSONObject.class, cronTrigger.getActiveTasks());
		Map<String, Integer> activeGroupsCount = timedExecutor.getActiveGroupsCount();
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "timed", new JSONArray(active));
		JsonUtil.addToJson(object, "cron", new JSONArray(cron));
		JsonUtil.addToJson(object, "activeGroups", JsonUtil.toJsonObject(activeGroupsCount));
		return Response.ok(object.toString()).build();
	}

	/**
	 * Fetches information for a single entry
	 *
	 * @param identifier
	 *            the identifier
	 * @return the response
	 */
	@GET
	@Path("/{identifier}")
	public Response get(@PathParam("identifier") String identifier) {

		SchedulerEntry entry = schedulerService.getScheduleEntry(identifier);
		if (entry == null) {
			return RestUtil.buildErrorResponse(Status.NOT_FOUND, "No entry found with identifier [" + identifier + "]");
		}
		List<SchedulerEntry> entries = Collections.singletonList(entry);
		return RestUtil.buildDataResponse(new JSONArray(typeConverter.convert(JSONObject.class, entries)));
	}

	/**
	 * Stop timer with cron expression and remove it from scheduler entry.
	 *
	 * <div style='color:red'> Definition with id <code>identifier</code> must be reloaded with
	 * attribute "isAbstract="true" to not solve scheduling again if definition is reloaded or server restarted!</div>
	 *
	 * @param identifier the identifier of timer.
	 * @return the response
	 */
	@POST
	@Path("stop/{identifier}")
	public Response stop(@PathParam("identifier") String identifier) {
		SchedulerEntry entry = schedulerService.getScheduleEntry(identifier);
		if (entry == null) {
			throw new ResourceNotFoundException(identifier);
		}
		entry.getConfiguration().setRemoveOnSuccess(true);
		entry.setStatus(SchedulerEntryStatus.COMPLETED);
		schedulerService.save(entry);
		return RestUtil.buildDataResponse(typeConverter.convert(JSONObject.class, entry));
	}

	/**
	 * Manually trigger entity with given identifier
	 *
	 * @param identifier the identifier of the entity to trygger
	 */
	@POST
	@Path("activate/{identifier}")
	public void activate(@PathParam("identifier") String identifier) {
		SchedulerEntry entry = schedulerService.getScheduleEntry(identifier);
		if (entry == null) {
			throw new ResourceNotFoundException(identifier);
		}
		SchedulerEntry activated = schedulerService.activate(entry.getId());
		timedExecutor.rescheduleEntry(activated);
	}
}
