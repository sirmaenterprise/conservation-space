package com.sirma.itt.sch.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.util.ModelConverter;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Rest services for project schedule operations.
 * 
 * @author svelikov
 */
@Path("/schedule/task")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class ScheduleTaskController {

	private final Logger log = Logger.getLogger(ScheduleTaskController.class);
	private final boolean debug = log.isDebugEnabled();

	private static final String TO = "To";
	private static final String FROM = "From";
	private static final String RECORD_ID = "recordId";
	private static final String PROJECT_ID = "projectId";
	private static final String TASKS = "tasks";
	private static final String TASK_ID = "TaskId";
	private static final String NODE = "node";
	private static final String ASSIGNMENTS = "assignments";
	private static final String RESOURCES = "resources";
	private static final String DEPENDENCIES = "dependencies";

	@Inject
	private ScheduleService scheduleService;

	@Inject
	private ScheduleResourceService resourceService;

	@Inject
	private ProjectService projectService;

	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> entryDao;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private ExpressionsManager evaluatorManager;

	@Inject
	private CodelistService codelistService;

	@Inject
	private StateTransitionManager stateTransitionManager;

	/**
	 * View.
	 * 
	 * @param node
	 *            the node
	 * @param projectId
	 *            the project id
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	@Path("view")
	@GET
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String view(@QueryParam(NODE) String node, @QueryParam(PROJECT_ID) String projectId)
			throws Exception {

		if (debug) {
			log.debug("ScheduleTaskController.view node [" + node + "], projectId [" + projectId
					+ "]");
		}

		String response = loadAll(node, projectId);

		if (debug) {
			log.debug("ScheduleTaskController.view: response [" + response + "]");
		}
		return response;
	}

	/**
	 * Load all data.
	 * 
	 * @param node
	 *            the node
	 * @param projectDbId
	 *            the project db id
	 * @return the string
	 * @throws JSONException
	 */
	protected String loadAll(String node, Serializable projectDbId) throws JSONException {
		ProjectInstance projectInstance = projectService.loadByDbId(projectDbId);
		ScheduleInstance scheduleInstance = scheduleService.getOrCreateSchedule(projectInstance);
		JSONArray taskStore = null;
		if (StringUtils.isNullOrEmpty(node)) {
			List<ScheduleEntry> entries = scheduleService.getEntries(scheduleInstance);

			taskStore = ModelConverter.buildTaskStore(entries, typeConverter);

			// build root object
			JSONObject response = new JSONObject();

			JSONObject scheduleConfig = new JSONObject();
			Set<String> allowedOperations = stateTransitionManager.getAllowedOperations(
					projectInstance,
					(String) projectInstance.getProperties().get(ProjectProperties.STATUS));
			boolean canEditSchedule = allowedOperations
					.contains(PmActionTypeConstants.EDIT_SCHEDULE);
			// set 'true' for activate schedule read-only
			scheduleConfig.put("disabled", !canEditSchedule);

			response.put("scheduleConfig", scheduleConfig);

			response.put("data", taskStore.get(0));

			return response.toString();
		}

		Long nodeId = Long.parseLong(node);
		List<ScheduleEntry> list = scheduleService.getChildren(nodeId);
		taskStore = ModelConverter.buildTaskStore(list, typeConverter);
		return taskStore.toString();
	}

	/**
	 * Commit task or multiple tasks.
	 * 
	 * @param data
	 *            selected task records
	 * @return the string
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Path("commit")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String commit(String data) throws JSONException {
		log.debug("ScheduleTaskController.commit request [" + data + "]");

		JSONObject object = new JSONObject(data);
		String projectId = JsonUtil.getStringValue(object, PROJECT_ID);
		ProjectInstance projectInstance = projectService.loadByDbId(projectId);
		ScheduleInstance scheduleInstance = scheduleService.getOrCreateSchedule(projectInstance);

		JSONArray array = (JSONArray) JsonUtil.getValueOrNull(object, TASKS);
		List<JSONObject> entries = new ArrayList<JSONObject>(array.length());
		for (int i = 0; i < array.length(); i++) {
			JSONObject element = (JSONObject) array.get(i);
			entries.add(element);
		}
		List<ScheduleEntry> collection = (List<ScheduleEntry>) typeConverter.convert(
				ScheduleEntry.class, entries);

		Map<Serializable, ScheduleEntry> scheduleEntriesMap = new HashMap<Serializable, ScheduleEntry>();
		for (ScheduleEntry scheduleEntry : collection) {
			scheduleEntriesMap.put(scheduleEntry.getId(), scheduleEntry);
		}

		// Already started entries
		List<ScheduleEntry> started = new ArrayList<ScheduleEntry>();
		// Entries to be started
		List<ScheduleEntry> toBeStarted = new ArrayList<ScheduleEntry>();
		// Same entries retrieved from the database. Used to check if they are already started by
		// someone else of by automatic scheduler
		List<ScheduleEntry> databaseEntries = entryDao.loadInstancesByDbKey(
				new ArrayList<Serializable>(scheduleEntriesMap.keySet()), false);
		for (ScheduleEntry scheduleEntry : databaseEntries) {
			// If entry is already started send updated instance to the client
			if (scheduleEntry.getActualInstanceId() != null) {
				started.add(scheduleEntry);
			} else {
				ScheduleEntry scheduleEntryToStart = scheduleEntriesMap.get(scheduleEntry.getId());
				if (scheduleEntryToStart != null) {
					if (scheduleEntryToStart.getActualInstanceClass() == WorkflowInstanceContext.class) {
						scheduleEntryToStart.getProperties().put(DefaultProperties.STATUS,
								"IN_PROGRESS");
					}
					toBeStarted.add(scheduleEntryToStart);
				}
			}
		}

		started.addAll(scheduleService.startEntries(scheduleInstance, toBeStarted));

		Collection<JSONObject> convert = typeConverter.convert(JSONObject.class, started);
		JSONArray result = new JSONArray(convert);

		log.debug("ScheduleTaskController.commit response [" + result + "]");
		return result.toString();
	}

	/**
	 * Update project schedule.
	 * 
	 * @param data
	 *            the data
	 * @return response data.
	 */
	@Path("update")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String update(String data) {
		log.debug("ScheduleTaskController.update [" + data + "]");

		JSONObject object = JsonUtil.createObjectFromString(data);

		String projectId = JsonUtil.getStringValue(object, PROJECT_ID);
		ProjectInstance instance = projectService.loadByDbId(projectId);
		ScheduleInstance schedule = scheduleService.getOrCreateSchedule(instance);

		Object value = JsonUtil.getValueOrNull(object, TASKS);
		List<ScheduleEntry> tasks = new LinkedList<ScheduleEntry>();
		Map<String, ScheduleEntry> nonPersistedTasks = new LinkedHashMap<String, ScheduleEntry>();
		Map<Long, JSONObject> incommingTasks = new LinkedHashMap<Long, JSONObject>();
		Map<Serializable, ScheduleEntry> toLoad = new LinkedHashMap<>();
		JSONArray template = null;
		if (value != null) {
			template = (JSONArray) value;
			for (int i = 0; i < template.length(); i++) {
				JSONObject jsonObject = (JSONObject) JsonUtil.getFromArray(template, i);
				ScheduleEntry scheduleEntry = typeConverter
						.convert(ScheduleEntry.class, jsonObject);
				scheduleEntry.setScheduleId((Long) schedule.getId());
				List<ScheduleEntry> entry;
				if (scheduleEntry.getId() == null) {
					entry = scheduleService.addEntry(scheduleEntry);
					// save by phantom id to be able to reference them later
					nonPersistedTasks.put(scheduleEntry.getPhantomId(), entry.get(0));
					JsonUtil.addToJson(jsonObject, ScheduleEntryProperties.ID, entry.get(0).getId());
					tasks.addAll(entry);
				} else {
					if (scheduleEntry.getActualInstanceId() != null) {
						toLoad.put(scheduleEntry.getId(), scheduleEntry);
					} else {
						entry = scheduleService.updateEntry(scheduleEntry);
						tasks.addAll(entry);
					}
				}
				incommingTasks.put(JsonUtil.getLongValue(jsonObject, ScheduleEntryProperties.ID),
						jsonObject);
			}
		}

		// fetch all old entries with a single call
		List<ScheduleEntry> oldCopies = entryDao.loadInstancesByDbKey(
				new ArrayList<>(toLoad.keySet()), false);
		for (ScheduleEntry old : oldCopies) {
			ScheduleEntry newEntry = toLoad.get(old.getId());
			// if the new parent is
			if (StringUtils.isNotNullOrEmpty(newEntry.getParentPhantomId())) {
				log.warn("Trying to change parent of started instance to instance that is not started!. Ignoring the request!");
				continue;
			} else if (!EqualsHelper.nullSafeEquals(old.getParentId(), newEntry.getParentId())) {
				// execute move logic and save the entry
				List<ScheduleEntry> list = scheduleService.moveEntry(newEntry);
				tasks.addAll(list);
			} else {
				scheduleService.updateEntry(newEntry);
			}
		}

		// convert parent phantom ids to actual IDs
		for (ScheduleEntry scheduleEntry : tasks) {
			if (StringUtils.isNotNullOrEmpty(scheduleEntry.getParentPhantomId())) {
				ScheduleEntry parentEntry = nonPersistedTasks.get(scheduleEntry
						.getParentPhantomId());
				if (parentEntry != null) {
					scheduleEntry.setParentId(parentEntry.getId());
					JSONObject jsonObject = incommingTasks.get(scheduleEntry.getId());
					if (jsonObject != null) {
						JsonUtil.addToJson(jsonObject, ScheduleEntryProperties.PARENT_ID, ""
								+ parentEntry.getId());
					}
					// Why we override tasks start and end dates???
					scheduleEntry.setStartDate(parentEntry.getStartDate());
					scheduleEntry.setEndDate(parentEntry.getEndDate());
					// save changes to DB
					entryDao.saveEntity(scheduleEntry);
				} else {
					log.warn("For task with Id=" + scheduleEntry.getId()
							+ " failed to find his parent with PhantomParentId="
							+ scheduleEntry.getParentPhantomId());
				}
			}
		}

		JSONArray taskStoreObj;
		if (template != null) {
			taskStoreObj = ModelConverter.buildTaskStoreAs(null, tasks, typeConverter);
		} else {
			taskStoreObj = new JSONArray();
		}
		// copies the properties from the incoming tasks to the result tree
		ModelConverter.syncProperties(incommingTasks, taskStoreObj);

		value = JsonUtil.getValueOrNull(object, ASSIGNMENTS);
		List<ScheduleAssignment> assignments = new LinkedList<ScheduleAssignment>();
		if (value != null) {
			JSONArray array = (JSONArray) value;
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = (JSONObject) JsonUtil.getFromArray(array, i);
				String taskId = JsonUtil.getStringValue(jsonObject, TASK_ID);
				if (taskId != null) {
					if (nonPersistedTasks.containsKey(taskId)) {
						JsonUtil.addToJson(jsonObject, TASK_ID, nonPersistedTasks.get(taskId)
								.getId());
					}
					ScheduleAssignment assignment = typeConverter.convert(ScheduleAssignment.class,
							jsonObject);
					assignment.setScheduleId((Long) schedule.getId());
					ScheduleAssignment saved = resourceService.save(assignment);
					assignments.add(saved);
				}
			}
		}

		value = JsonUtil.getValueOrNull(object, DEPENDENCIES);
		List<ScheduleDependency> dependencies = new LinkedList<ScheduleDependency>();
		if (value != null) {
			JSONArray array = (JSONArray) value;
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = (JSONObject) JsonUtil.getFromArray(array, i);
				String fromId = JsonUtil.getStringValue(jsonObject, FROM);
				if (nonPersistedTasks.containsKey(fromId)) {
					JsonUtil.addToJson(jsonObject, FROM, nonPersistedTasks.get(fromId).getId());
				}

				String to = JsonUtil.getStringValue(jsonObject, TO);
				if (nonPersistedTasks.containsKey(to)) {
					JsonUtil.addToJson(jsonObject, TO, nonPersistedTasks.get(to).getId());
				}

				ScheduleDependency dependency = typeConverter.convert(ScheduleDependency.class,
						jsonObject);
				Long fromDependency = dependency.getFrom();
				if (nonPersistedTasks.containsKey(fromDependency)) {
					dependency.setFrom((Long) nonPersistedTasks.get(fromDependency).getId());
				}
				Long toDependency = dependency.getTo();
				if (nonPersistedTasks.containsKey(toDependency)) {
					dependency.setTo((Long) nonPersistedTasks.get(toDependency).getId());
				}
				dependency.setScheduleId((Long) schedule.getId());
				ScheduleDependency saved = resourceService.save(dependency);
				dependencies.add(saved);
			}
		}

		JSONObject response = new JSONObject();
		JsonUtil.addToJson(response, RESOURCES, new JSONArray());
		JsonUtil.addToJson(response, ASSIGNMENTS,
				new JSONArray(typeConverter.convert(JSONObject.class, assignments)));
		JsonUtil.addToJson(response, DEPENDENCIES,
				new JSONArray(typeConverter.convert(JSONObject.class, dependencies)));
		JsonUtil.addToJson(response, TASKS, taskStoreObj);

		log.debug("ScheduleTaskController.update response [" + response + "]");
		return response.toString();
	}

	/**
	 * Method that retrieve all schedule entries by IDs and remove them from the database.
	 * 
	 * @param data
	 *            received data from AJAX request consisting of 3 {@link JSONArray}s with
	 *            assignment, dependency and task ids.
	 * @return JSON string
	 * @throws Exception
	 */

	@Path("delete")
	@POST
	public String delete(String data) {
		log.debug("ScheduleTaskController.delete [" + data + "]");
		JSONObject object = JsonUtil.createObjectFromString(data);
		buildAssignments(object);
		buildDependencies(object);
		buildTasks(object);
		return "success";
	}

	/**
	 * Builds the tasks array.
	 * 
	 * @param object
	 *            the object
	 */
	private void buildTasks(JSONObject object) {
		List<Long> tasksIds = getScheduleResources(object, TASKS);
		// Is load all properties needed?
		List<ScheduleEntry> tasks2delete = entryDao.loadInstancesByDbKey(tasksIds, true);
		for (ScheduleEntry scheduleEntry : tasks2delete) {
			scheduleService.deleteEntry(scheduleEntry);
		}
	}

	/**
	 * Builds the dependencies array.
	 * 
	 * @param object
	 *            the object
	 */
	private void buildDependencies(JSONObject object) {
		List<Long> dependenciesIds = getScheduleResources(object, DEPENDENCIES);
		List<ScheduleDependency> dependencies2delete = resourceService
				.getDependencies(dependenciesIds);
		for (ScheduleDependency scheduleDependency : dependencies2delete) {
			resourceService.delete(scheduleDependency);
		}
	}

	/**
	 * Builds the assignments array.
	 * 
	 * @param object
	 *            the object
	 */
	private void buildAssignments(JSONObject object) {
		List<Long> assignmentsIds = getScheduleResources(object, ASSIGNMENTS);
		List<ScheduleAssignment> assignments2delete = resourceService
				.getAssignments(assignmentsIds);
		for (ScheduleAssignment scheduleAssignment : assignments2delete) {
			resourceService.delete(scheduleAssignment);
		}
	}

	/**
	 * Collect ids.
	 * 
	 * @param tasks
	 *            the tasks
	 * @return the list
	 */
	private List<Long> collectIds(JSONArray tasks) {
		List<Long> tasksIds = new ArrayList<Long>();
		for (int i = 0; i < tasks.length(); i++) {
			tasksIds.add(Long.valueOf((String) JsonUtil.getFromArray(tasks, i)));
		}
		return tasksIds;
	}

	/**
	 * Gets the schedule resources by type.
	 * 
	 * @param object
	 *            the object
	 * @param resourceType
	 *            the resource type
	 * @return the schedule resources
	 */
	private List<Long> getScheduleResources(JSONObject object, String resourceType) {
		JSONArray tasks = (JSONArray) JsonUtil.getValueOrNull(object, resourceType);
		List<Long> resources = CollectionUtils.emptyList();
		if ((tasks != null) && (tasks.length() > 0)) {
			resources = collectIds(tasks);
		}
		return resources;
	}

	/**
	 * Cancel schedule entry creation.
	 * 
	 * @param data
	 *            the data
	 * @return response string
	 */
	@Path("cancel")
	@POST
	public String cancel(String data) {
		log.debug("ScheduleTaskController.cancel [" + data + "]");
		JSONObject object = JsonUtil.createObjectFromString(data);

		// this will hold elements id-s
		List<Long> elementsId = new ArrayList<Long>();

		// all records id-s
		JSONArray receivedData = (JSONArray) JsonUtil.getValueOrNull(object, RECORD_ID);

		for (int i = 0; i < receivedData.length(); i++) {
			// parsing and adding to the id holder
			elementsId.add(Long.valueOf((String) JsonUtil.getFromArray(receivedData, i)));
		}

		// retrieve all ScheduleInstance based on records id-s
		List<ScheduleEntry> collection = entryDao.loadInstancesByDbKey(elementsId, true);

		Iterator<ScheduleEntry> iterator = collection.iterator();
		while (iterator.hasNext()) {
			// cancel the entry
			scheduleService.cancelEntry(iterator.next());
		}

		return "success";
	}

	/**
	 * Gets the allowed children.
	 * 
	 * @param data
	 *            the data
	 * @return the allowed children
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Path("allowedChildren")
	@POST
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getAllowedChildren(String data) throws JSONException {
		if (debug) {
			log.debug("ScheduleTaskController.getAllowedChildren input[" + data + "]");
		}
		JSONObject object = new JSONObject(data);
		String language = JsonUtil.getStringValue(object, "language");
		if (language == null) {
			language = "en";
		}
		try {
			// enable recursive schedule entry conversion
			RuntimeConfiguration.setConfiguration(
					RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION, Boolean.TRUE);
			ScheduleEntry parent = typeConverter.convert(ScheduleEntry.class, object);

			Map<String, List<DefinitionModel>> allowedChildrenForNode = scheduleService
					.getAllowedChildrenForNode(parent, parent.getChildren());

			JSONObject store = ModelConverter.buildAllowedChildrenStore(allowedChildrenForNode,
					evaluatorManager, codelistService, language);

			if (debug) {
				log.debug("ScheduleTaskController.getAllowedChildren result[" + store + "]");
			}
			return store.toString();
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.USE_RECURSIVE_CONVERSION);
		}
	}

}
