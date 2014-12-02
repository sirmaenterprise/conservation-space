package com.sirma.itt.sch.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.CurrentUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Used to serve and process data to/from Resource allocation view.
 *
 * @author iborisov
 */
@Path("/resourceAllocation/task")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class ResourceAllocationController {

	private static final String PROJECT_ID = "projectId";
	private static final String VIEW_OTHER_PROJECTS = "viewOtherProjects";
	private static final String USERNAMES = "usernames";

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The resource service. */
	@Inject
	private ScheduleResourceService scheduleResourceService;

	/** The project service. */
	@Inject
	private ProjectService projectService;
	
	@Inject
	private SearchService searchService;
	
	/** The current user. */
	@Inject
	@CurrentUser
	private User currentUser;
	
	@Inject
	private Logger logger;
	
	/**
	 * Returns resources allocation based on input criteria.
	 *
	 * @param data filter data in json format
	 * @return json formatted result
	 * @throws Exception the exception
	 */
	@Path("filter")
	@POST
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String filter(String data) throws Exception {
		JSONArray taskStore = new JSONArray();

		JSONObject object = new JSONObject(data);
		String projectId = JsonUtil.getStringValue(object, PROJECT_ID);
		Boolean viewOtherProjects = JsonUtil.getBooleanValue(object, VIEW_OTHER_PROJECTS);
		String usernames = JsonUtil.getStringValue(object, USERNAMES);
		
		Set<Serializable> resourcesToDisplay = null;
		if (!StringUtils.isNullOrEmpty(usernames)) {
			String[] split = usernames.split(",");
			resourcesToDisplay = new HashSet<>(split.length);
			for (String username : split) {
				resourcesToDisplay.add(username);
			}
		} else {
			resourcesToDisplay = new HashSet<>();
		}
		
		viewOtherProjects = viewOtherProjects == null ? false : viewOtherProjects;

		List<Instance> allProjects = null;
		if (viewOtherProjects || StringUtils.isNullOrEmpty(projectId)) {
			Context<String, Object> context = new Context<>();
			context.put(SearchFilterProperties.USER_ID, currentUser.getIdentifier());
			SearchArguments<? extends Instance> arguments = searchService.getFilter("visibleProject",
					ProjectInstance.class, context);
			arguments.setPageSize(Integer.MAX_VALUE);
			arguments.setMaxSize(Integer.MAX_VALUE);
			searchService.search(ProjectInstance.class, arguments);
			List<? extends Instance> result = arguments.getResult();
			allProjects = new ArrayList<>(result.size());
			// If we are in a project's context move current project to the top of the list. Used to calculate date range if displaying assignments from other projects.
			if (!StringUtils.isNullOrEmpty(projectId)) {
				for (Instance instance : result) {
					if (instance.getId().equals(projectId)) {
						allProjects.add(0, instance);
					} else {
						allProjects.add(instance);
					}
				}
			} else {
				allProjects.addAll(result);
			}
		} else {
			allProjects = new ArrayList<>();
			allProjects.add(projectService.loadByDbId(projectId));
		}
		
		// Map holding the relations between resource and its assignments.
		Map<Resource, JSONArray> resourceToAssignments = new HashMap<Resource, JSONArray>();
		// Map holding the relations between resource and its date range (based on assignments).
		Map<Resource, DateRange> resourceToDateRange = new HashMap<Resource, DateRange>();
		DateRange currentProjectDateRange = null;
		for (Instance projectInstance : allProjects) {
			ScheduleInstance scheduleInstance = scheduleService.getOrCreateSchedule(projectInstance);			
			List<Resource> resources = scheduleResourceService.getResources(projectInstance, scheduleInstance.getId());
			
			// If it is current project and there are no explicit usernames set add unassinged tasks to result.
			if (StringUtils.isNotNullOrEmpty(projectId) && projectInstance.getId().equals(projectId) 
					&& StringUtils.isNullOrEmpty(usernames)) {
				Map<String, List<ScheduleEntry>> unassignedAssignments = scheduleService.searchAssignments(scheduleInstance, null, null);
				JSONObject unassignedJSON = new JSONObject();
				unassignedJSON.put("Id", "r_-1");
				unassignedJSON.put("ResourceName", "Unassigned");
				unassignedJSON.put("children", convertAssignmentsToJSONArray(unassignedAssignments.get("-1"), projectInstance));
				taskStore.put(unassignedJSON);				
			}
			
			List<Serializable> resourceIds = new ArrayList<Serializable>();
			Iterator<Resource> iterator = resources.iterator();
			while (iterator.hasNext()) {
				Resource resource = iterator.next();
				// There are usernames defined. Use them to filter results 
				if (!StringUtils.isNullOrEmpty(usernames)) {
					if (resourcesToDisplay.contains(resource.getId()) || resourcesToDisplay.contains(resource.getIdentifier())) {
						resourceIds.add(resource.getId());
					} else {
						iterator.remove();
					}
				}
				// There are no usernames defined but we have a project. Display only resources available in the project
				else if (StringUtils.isNotNullOrEmpty(projectId)) {
					// If this is current project add to resources to display
					if (projectInstance.getId().equals(projectId)) {
						resourcesToDisplay.add(resource.getId());
						resourceIds.add(resource.getId());
					} else if (resourcesToDisplay.contains(resource.getId())) {
						resourceIds.add(resource.getId());
					} else {
						iterator.remove();
					}
				} 
				// Display all results
				else {
					resourceIds.add(resource.getId());
				}
			}
			
			Map<String, List<ScheduleEntry>> assignedAssignments = scheduleService.searchAssignments(scheduleInstance, resourceIds, currentProjectDateRange);
			for (Resource resource : resources) {
				List<ScheduleEntry> resourceAssignments = assignedAssignments.get(resource.getId());
				
				JSONArray assignments = resourceToAssignments.get(resource);
				if (assignments == null) {
					assignments = new JSONArray();
					resourceToAssignments.put(resource, assignments);
				}
				JSONArray convertAssignmentsToJSONArray = convertAssignmentsToJSONArray(resourceAssignments, projectInstance);
				for (int i = 0; i < convertAssignmentsToJSONArray.length(); i++) {
					assignments.put(convertAssignmentsToJSONArray.get(i));
				}
				
				// Calculate date range. Accumulate values per users for different projects.
				DateRange resourceDateRange = resourceToDateRange.get(resource);
				if (resourceDateRange == null) {
					resourceDateRange = new DateRange(null, null);
					resourceToDateRange.put(resource, resourceDateRange);
				}				
				DateRange resourceStartEndDate = getOverallStartEndDates(resourceAssignments, resourceDateRange);
				
				if (!StringUtils.isNullOrEmpty(projectId) && projectInstance.getId().equals(projectId)) {
					if (currentProjectDateRange == null) {
						currentProjectDateRange = new DateRange(null, null);
					}

					if ((resourceStartEndDate.getFirst() != null) && ((currentProjectDateRange.getFirst() == null) || resourceStartEndDate.getFirst().before(currentProjectDateRange.getFirst()))) {
						currentProjectDateRange.setFirst(resourceStartEndDate.getFirst());
					}

					if ((resourceStartEndDate.getSecond() != null) && ((currentProjectDateRange.getSecond() == null) || resourceStartEndDate.getSecond().after(currentProjectDateRange.getSecond()))) {
						currentProjectDateRange.setSecond(resourceStartEndDate.getSecond());
					}
				}
			}
		}
		
		for (Entry<Resource, JSONArray> entry : resourceToAssignments.entrySet()) {
			Resource resource = entry.getKey();
			
			JSONObject resourceJSON = new JSONObject();
			resourceJSON.put("Id", resource.getId());
			resourceJSON.put("ResourceName", StringUtils.isNullOrEmpty(resource.getDisplayName()) ? resource.getIdentifier() : resource.getDisplayName());
			resourceJSON.put("cls", "user");
			resourceJSON.put("children", entry.getValue());

			DateRange resourceDates = resourceToDateRange.get(resource);
			if (resourceDates.getFirst() != null) {
				resourceJSON.put("StartDate", TypeConverterUtil.getConverter().convert(String.class, resourceDates.getFirst()));
			}
			if (resourceDates.getSecond() != null) {
				resourceJSON.put("EndDate", TypeConverterUtil.getConverter().convert(String.class, resourceDates.getSecond()));
			}
			taskStore.put(resourceJSON);
		}

		JSONObject rootJSON = new JSONObject();
		rootJSON.put("Id", "root");
		rootJSON.put("ResourceName", "Resources");
		rootJSON.put("children", taskStore);

		return rootJSON.toString();
	}

	/**
	 * Convert assignments to json array.
	 *
	 * @param assignments List of assignments to be converted
	 * @param projectInstance the instance of the current project
	 * @return the jSON array
	 * @throws JSONException the jSON exception
	 */
	private JSONArray convertAssignmentsToJSONArray(List<ScheduleEntry> assignments, Instance projectInstance) throws JSONException {
		JSONArray assignmensJSON = new JSONArray();
		Serializable projectTitle = projectInstance.getProperties()!=null?projectInstance.getProperties().get(DefaultProperties.TITLE):"";
		projectTitle = projectTitle==null?"":projectTitle;

		if (assignments != null) {
			for (ScheduleEntry assignment : assignments) {
				Map<String, Serializable> properties = assignment.getProperties();

				JSONObject assignementJSON = new JSONObject();
				assignementJSON.put("Id", "a_" + assignment.getId());
				Serializable title = properties.get(DefaultProperties.TITLE);
				assignementJSON.put("ProjectTitle", projectTitle);
				assignementJSON.put("ProjectId", projectInstance.getId());
				assignementJSON.put("ResourceName", (title == null ? ""	: title.toString()));
				Serializable duration = properties.get(ScheduleEntryProperties.DURATION);
				assignementJSON.put("Duration", (duration == null ? "0"	: duration.toString()));
				assignementJSON.put("StartDate", TypeConverterUtil.getConverter().convert(String.class, assignment.getStartDate()));
				assignementJSON.put("EndDate", TypeConverterUtil.getConverter().convert(String.class, assignment.getEndDate()));
				Serializable status = properties.get(DefaultProperties.STATUS);
				assignementJSON.put("Status", (status == null ? ""	: status.toString()));
				assignementJSON.put("leaf", true);
				assignementJSON.put("cls", assignment.getInstanceReference().getReferenceType().getName());
				assignementJSON.put("aiid", assignment.getInstanceReference().getIdentifier());
				assignmensJSON.put(assignementJSON);
			}
		}
		return assignmensJSON;
	}

	/**
	 * Gets the overall start and end dates based on the first assignment start date and last assignment end date.
	 *
	 * @param assignments the assignments
	 * @param dateRange date range to be used as a base. Use existing date range to accumulate values or null for new.
	 * @return the overall start end dates
	 * @throws JSONException the jSON exception
	 */
	private DateRange getOverallStartEndDates(List<ScheduleEntry> assignments, DateRange dateRange) throws JSONException {
		if (dateRange == null) {
			dateRange = new DateRange(null, null);
		}
		if (assignments != null) {
			for (ScheduleEntry assignment : assignments) {
				Date startDate = assignment.getStartDate();

				if ((startDate != null) && ((dateRange.getFirst() == null) || startDate.before(dateRange.getFirst()))) {
					dateRange.setFirst(startDate);
				}

				Date endDate = assignment.getEndDate();
				if ((endDate != null) && ((dateRange.getSecond() == null) || endDate.after(dateRange.getSecond()))) {
					dateRange.setSecond(endDate);
				}
			}
		}
		return dateRange;
	}

}


