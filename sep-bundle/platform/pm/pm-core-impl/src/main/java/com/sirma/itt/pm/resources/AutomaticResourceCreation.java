package com.sirma.itt.pm.resources;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.pm.security.PmSecurityModel.PmRoles.PROJECT_MANAGER;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectCreatedEvent;

/**
 * Observer class that listens for projected creation events to add the initial users to the project
 * as managers. Listens also for task persist events. If the tasks is part of a project then the
 * users associated with it should also be added to the project if not already as
 * {@link com.sirma.itt.pm.security.PmSecurityModel#PROJECT_NON_MEMBER}.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class AutomaticResourceCreation {

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	@Inject
	private TaskService taskService;

	/**
	 * On create project.
	 * 
	 * @param event
	 *            the event
	 */
	public void onCreateProject(@Observes ProjectCreatedEvent event) {
		ProjectInstance instance = event.getInstance();

		String owner = (String) instance.getProperties().get(ProjectProperties.OWNER);
		String creator = (String) instance.getProperties().get(ProjectProperties.CREATED_BY);
		Map<Resource, RoleIdentifier> assignable = new HashMap<>();
		// add the creator
		assignable.put(resourceService.getResource(creator, ResourceType.USER), PROJECT_MANAGER);
		if (owner != null) {
			assignable.put(resourceService.getResource(owner, ResourceType.USER), PROJECT_MANAGER);
		}
		resourceService.assignResources(instance, assignable);
	}

	/**
	 * On task creation.
	 * 
	 * @param event
	 *            the event
	 */
	public void onTaskCreation(@Observes AfterInstancePersistEvent<Instance, TwoPhaseEvent> event) {
		Instance task = event.getInstance();
		if (task instanceof AbstractTaskInstance) {
			// we should try to minimize creating new instances and also for multiple instances
			// created at once like workflows and their tasks when parent instance could not be
			// fetched, yet.
			Instance parent = InstanceUtil.getRootInstance(task, true);
			if (parent instanceof ProjectInstance) {
				ProjectInstance projectInstance = (ProjectInstance) parent;
				Pair<Set<String>, Set<String>> poolResources = null;
				Set<String> users = new LinkedHashSet<>();
				Set<String> groups = new LinkedHashSet<>();
				if ((task.getProperties().get(TaskProperties.TASK_OWNER) == null)
						&& ((poolResources = taskService
								.getPoolResources((AbstractTaskInstance) task)) != null)) {
					if (poolResources.getFirst() != null) {
						groups.addAll(poolResources.getFirst());
					}
					if (poolResources.getSecond() != null) {
						users.addAll(poolResources.getSecond());
					}
				} else {
					String assignee = (String) task.getProperties().get(
							TaskProperties.TASK_ASSIGNEE);
					if (StringUtils.isNullOrEmpty(assignee)) {
						assignee = (String) task.getProperties().get(TaskProperties.TASK_OWNER);
					}
					if (StringUtils.isNotNullOrEmpty(assignee)) {
						users.add(assignee);
					}
				}
				if (users.isEmpty() && groups.isEmpty()) {
					return;
				}
				Map<Resource, RoleIdentifier> assignable = new HashMap<>();
				for (String user : users) {
					// if the resource is not a resource yet so the project does not contains the
					// resource also
					Resource resource = resourceService.getResource(user, ResourceType.USER);
					ResourceRole resourceRole = resourceService.getResourceRole(projectInstance,
							resource);
					if (resourceRole == null) {
						assignable.put(resource, CONSUMER);
						// resourceService.assignResource(resource, CONSUMER, projectInstance);
					}
				}
				for (String group : groups) {
					// if the resource is not a resource yet so the project does not contains the
					// resource also
					Resource resource = resourceService.getResource(group, ResourceType.GROUP);
					ResourceRole resourceRole = resourceService.getResourceRole(projectInstance,
							group, ResourceType.GROUP);
					if (resourceRole == null) {
						assignable.put(resource, CONSUMER);
						// resourceService.assignResource(resource, CONSUMER, projectInstance);
					}
				}
				if (!assignable.isEmpty()) {
					resourceService.assignResources(parent, assignable);
				}
			}
		}
	}
}
