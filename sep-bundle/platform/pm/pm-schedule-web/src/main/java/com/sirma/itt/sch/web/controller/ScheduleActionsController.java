package com.sirma.itt.sch.web.controller;

import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.security.ScheduleActions;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.util.ModelConverter;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Schedule REST service to for actions evaluation.
 * 
 * @author svelikov
 */
@Path("/schedule/actions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScheduleActionsController {

	/** The Constant PARENT_ENTRY. */
	private static final String PARENT_ENTRY = "parentEntry";
	/** The Constant ENTRY. */
	private static final String ENTRY = "entry";
	/** The Constant ENTRY_ID. The id of the entry which is targeted by user when multiple entries are selected. */
	private static final String ENTRY_ID = "entryId";
	/** The Constant ENTRIES. */
	private static final String ENTRIES = "entries";
	/** The Constant PROJECT_ID. */
	private static final String PROJECT_ID = "projectId";

	/** The log. */
	private Logger log = Logger.getLogger(ScheduleActionsController.class);

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The project service. */
	@Inject
	private ProjectService projectService;

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Evaluate actions.
	 * 
	 * @param data
	 *            request data
	 * @return the response
	 */
	@Path("evaluate")
	@POST
	@Secure
	public Response evaluateActions(String data) {

		log.debug("ScheduleActionsController.evaluateActions request " + data);

		if (StringUtils.isNullOrEmpty(data)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		JSONObject request;
		try {
			request = new JSONObject(data);
		} catch (JSONException e) {
			log.error("", e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		if (!request.has(PROJECT_ID) || !request.has(ENTRY)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		ProjectInstance projectInstance = null;
		ScheduleEntry scheduleEntry = null;
		try {
			String projectIdentifier = JsonUtil.getStringValue(request, PROJECT_ID);

			projectInstance = projectService.loadByDbId(projectIdentifier);
			if (projectInstance == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		RuntimeConfiguration.setConfiguration(
				RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION, Boolean.TRUE);
		try {
			ScheduleInstance scheduleInstance = scheduleService
					.getOrCreateSchedule(projectInstance);

			Object entryData = JsonUtil.getValueOrNull(request, ENTRY);
			if (entryData instanceof JSONObject) {
				scheduleEntry = typeConverter.convert(ScheduleEntry.class, entryData);
			} else {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			ScheduleEntry parentEntry = null;
			if (request.has(PARENT_ENTRY)) {
				Object value = JsonUtil.getValueOrNull(request, PARENT_ENTRY);
				if (value instanceof JSONObject) {
					parentEntry = typeConverter.convert(ScheduleEntry.class, value);
				}
			}
			if (parentEntry != null) {
				int indexOf = parentEntry.getChildren().indexOf(scheduleEntry);

				if (indexOf >= 0) {
					parentEntry.getChildren().set(indexOf, scheduleEntry);
					scheduleEntry.setParentInstance(parentEntry);
					setScheduleInstance(parentEntry, scheduleInstance);
				} else {
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
			} else {
				setScheduleInstance(scheduleEntry, scheduleInstance);
			}

			Set<Action> actions = authorityService.getAllowedActions(scheduleEntry, "schedule");

			JSONObject model = ModelConverter.buildActions(scheduleEntry, actions);
			Response response = Response.status(Response.Status.OK).entity(model.toString())
					.build();
			return response;
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION);
		}
	}

	/**
	 * Evaluate common allowed actions for multiple selected entries. For now only Commit, Delete
	 * and Cancel operations can be performed over multiple nodes.
	 * 
	 * @param data
	 *            request data in the following format
	 *            <code>{projectId:'', entryId:'', entries:[{entry:{...}, parentEntry:{...}}, {...}, ...]}</code>
	 *            where <br>
	 *            <code>entryId</code> is the entry on which the right button is pressed <br>
	 *            <code>entries</code> is an array with all selected entries
	 * @return the response
	 */
	@Path("evaluateMultiple")
	@POST
	@Secure
	public Response evaluateActionsMultiple(String data) {
		log.debug("ScheduleActionsController.evaluateActionsMultiple request " + data);

		if (StringUtils.isNullOrEmpty(data)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		JSONObject request;
		try {
			request = new JSONObject(data);
		} catch (JSONException e) {
			log.error("", e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		if (!request.has(PROJECT_ID) || !request.has(ENTRIES) || !request.has(ENTRY_ID)) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		String entryId = JsonUtil.getStringValue(request, ENTRY_ID);
		ProjectInstance projectInstance = null;
		try {
			String projectIdentifier = JsonUtil.getStringValue(request, PROJECT_ID);

			projectInstance = projectService.loadByDbId(projectIdentifier);
			if (projectInstance == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		Set<Action> actionsIntersection = null;
		ScheduleEntry baseEntry = null;

		ScheduleInstance scheduleInstance = scheduleService
				.getOrCreateSchedule(projectInstance);

		Object entriesData = JsonUtil.getValueOrNull(request, ENTRIES);
		if (entriesData instanceof JSONArray) {
			JSONArray entriesDataArray = (JSONArray) entriesData;
			for (int i = 0; i < entriesDataArray.length(); i++) {
				try {
					Object entryObjectData = entriesDataArray.get(i);
					if (entryObjectData instanceof JSONObject) {
						JSONObject entryJSONObjectData = (JSONObject) entryObjectData;

						ScheduleEntry scheduleEntry = null;
						Object entryData = JsonUtil.getValueOrNull(entryJSONObjectData, ENTRY);
						if (entryData instanceof JSONObject) {
							scheduleEntry = typeConverter.convert(ScheduleEntry.class, entryData);
							if (entryId.equals(String.valueOf(scheduleEntry.getId())) || entryId.equals(scheduleEntry.getPhantomId())) {
								baseEntry = scheduleEntry;
							}
						} else {
							return Response.status(Response.Status.BAD_REQUEST).build();
						}

						ScheduleEntry parentEntry = null;
						if (entryJSONObjectData.has(PARENT_ENTRY)) {
							Object value = JsonUtil.getValueOrNull(entryJSONObjectData, PARENT_ENTRY);
							if (value instanceof JSONObject) {
								parentEntry = typeConverter.convert(ScheduleEntry.class, value);
							}
						}

						if (parentEntry != null) {
							parentEntry.getChildren().add(scheduleEntry);
							scheduleEntry.setParentInstance(parentEntry);
							setScheduleInstance(parentEntry, scheduleInstance);
						} else {
							setScheduleInstance(scheduleEntry, scheduleInstance);
						}

						Set<Action> actions = authorityService.getAllowedActions(scheduleEntry, "schedule");
						if (actionsIntersection == null) {
							actionsIntersection = actions;
						} else {
							actionsIntersection.retainAll(actions);
						}
					} else {
						return Response.status(Response.Status.BAD_REQUEST).build();
					}
				} catch (JSONException e) {
					log.error("", e);
					return Response.status(Response.Status.BAD_REQUEST).build();
				}
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		if ((baseEntry != null) && (actionsIntersection != null)) {
			// For now the only actions which are performed over multiple nodes are Commit, Delete, Cancel
			Iterator<Action> iter = actionsIntersection.iterator();
			while (iter.hasNext()) {
				Action action = iter.next();
				if (!ScheduleActions.APPROVE.equals(action)
						&& !ScheduleActions.DELETE.equals(action)
						&&
						!ScheduleActions.STOP.equals(action) ) {
					iter.remove();
				}
			}
			JSONObject model = ModelConverter.buildActions(baseEntry, actionsIntersection);
			Response response = Response.status(Response.Status.OK).entity(model.toString())
					.build();
			return response;
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	/**
	 * Sets the schedule instance.
	 * 
	 * @param entry
	 *            the entry
	 * @param scheduleInstance
	 *            the schedule instance
	 */
	private void setScheduleInstance(ScheduleEntry entry, ScheduleInstance scheduleInstance) {
		if (entry != null) {
			entry.setSchedule(scheduleInstance);
			for (ScheduleEntry scheduleEntry : entry.getChildren()) {
				setScheduleInstance(scheduleEntry, scheduleInstance);
			}
		}
	}
}
