package com.sirma.itt.sch.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.model.ScheduleResourceRole;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.util.ModelConverter;
import com.sirma.itt.pm.services.ProjectService;

/**
 * The Class ScheduleResourceController.
 *
 * @author svelikov
 */
@Path("/schedule/resourse")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScheduleResourceController {

	/** The Constant PROJECT_ID. */
	private static final String PROJECT_ID = "projectId";

	/** The log. */
	private Logger log = Logger.getLogger(ScheduleResourceController.class);

	/** The debug. */
	private boolean debug = log.isDebugEnabled();

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The schedule resource service. */
	@Inject
	private ScheduleResourceService scheduleResourceService;

	@Inject
	private ResourceService resourceService;
	/** The project service. */
	@Inject
	private ProjectService projectService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Loading dependencies.
	 *
	 * @param projectId
	 *            the project id
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	@Path("dependency")
	@GET
	public String loadDependencies(@QueryParam(PROJECT_ID) String projectId) throws Exception {
		if (debug) {
			log.debug("ScheduleResourceController.loadDependencies dependencies for projectId ["
					+ projectId + "]");
		}

		ProjectInstance projectInstance = projectService.loadByDbId(projectId);
		ScheduleInstance scheduleInstance = getScheduleInstance(projectInstance);

		List<ScheduleDependency> dependencies = scheduleResourceService
				.getDependencies(scheduleInstance.getId());

		Collection<JSONObject> collection = typeConverter.convert(JSONObject.class, dependencies);
		String dependencyStore = new JSONArray(collection).toString();

		if (debug) {
			log.debug("ScheduleDependencyController.loadDependencies: response [" + dependencyStore
					+ "]");
		}
		return dependencyStore;
	}

	/**
	 * Load assignments.
	 *
	 * @param projectId
	 *            the project id
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	@Path("assignment")
	@GET
	public String loadAssignments(@QueryParam(PROJECT_ID) String projectId) throws Exception {
		if (debug) {
			log.debug("ScheduleResourceController.loadAssignments assignments for projectId ["
					+ projectId + "]");
		}

		ProjectInstance projectInstance = projectService.loadByDbId(projectId);
		ScheduleInstance scheduleInstance = getScheduleInstance(projectInstance);

		List<ScheduleAssignment> assignments = scheduleResourceService
				.getAssignments(scheduleInstance.getId());
		// List<ScheduleResource> resources = scheduleResourceService.getResources(projectInstance,
		// scheduleInstance.getId());
		List<ResourceRole> roles = resourceService.getResourceRoles(projectInstance);
		Set<Resource> processed = new HashSet<Resource>();
		Map<Resource, ScheduleResourceRole> finalRole = new HashMap<Resource, ScheduleResourceRole>();
		List<ScheduleResourceRole> schRoles = new ArrayList<ScheduleResourceRole>(roles.size());
		for (ResourceRole role : roles) {
			if (role.getResource().getType() == ResourceType.GROUP) {
				List<Resource> containedResources = resourceService.getContainedResources(role
						.getResource());
				for (Resource resource : containedResources) {
					if (finalRole.containsKey(resource)) {
						continue;
					}
					ResourceRole highestRole = role;
					// create a role for each resource - if processed get the highest
					if (processed.contains(resource)) {
						if (!setHighestRole(projectInstance, finalRole, resource)) {
							continue;
						}
						highestRole = finalRole.get(resource).getResourceRole();
					} else {
						processed.add(resource);
					}
					ResourceRole resourceRole = new ResourceRole();
					resourceRole.setResource(resource);
					resourceRole.setRole(highestRole.getRole());
					resourceRole.setId(-1L);
					schRoles.add(new ScheduleResourceRole(resourceRole));
				}

			} else {
				ResourceRole highestRole = role;
				Resource resource = highestRole.getResource();
				if (finalRole.containsKey(resource)) {
					continue;
				}
				if (processed.contains(resource)) {
					if (!setHighestRole(projectInstance, finalRole, resource)) {
						continue;
					}
					highestRole = finalRole.get(resource).getResourceRole();
				} else {
					processed.add(resource);
				}
				schRoles.add(new ScheduleResourceRole(highestRole));
			}
		}
		processed = null;
		JSONObject assignmentStore = ModelConverter.buildScheduleAssignmentStore(assignments,
				schRoles);

		if (debug) {
			log.debug("ScheduleDependencyController.loadAssignments: response [" + assignmentStore
					+ "]");
		}
		return assignmentStore.toString();
	}

	/**
	 * Sets the highest role in the cache and returns true if this is newly set role
	 *
	 * @param rootInstance
	 *            is the root to check assignment as
	 * @param finalRole
	 *            is the cache
	 * @param resource
	 *            is the resource
	 * @return true if this is newly set role.
	 */
	private boolean setHighestRole(ProjectInstance rootInstance,
			Map<Resource, ScheduleResourceRole> finalRole, Resource resource) {
		ResourceRole highestRole = null;
		// get the highest
		if (!finalRole.containsKey(resource)) {
			highestRole = resourceService.getResourceRole(rootInstance, resource);
			finalRole.put(resource, new ScheduleResourceRole(highestRole));
			return true;
		}
		return false;
	}

	/**
	 * Gets the schedule instance.
	 *
	 * @param projectInstance
	 *            the project instance
	 * @return the schedule instance
	 */
	protected ScheduleInstance getScheduleInstance(ProjectInstance projectInstance) {
		return scheduleService.getOrCreateSchedule(projectInstance);
	}

}
