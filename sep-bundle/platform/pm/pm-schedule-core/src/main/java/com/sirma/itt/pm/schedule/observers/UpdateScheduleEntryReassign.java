package com.sirma.itt.pm.schedule.observers;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskClaimEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskReassignEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskReleaseEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskReassignEvent;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;

/**
 * Observer class that listens for task reassign events to update the schedule reassign users.
 *
 * @author BBonev
 */
@ApplicationScoped
public class UpdateScheduleEntryReassign {

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The resource service. */
	@Inject
	private ScheduleResourceService scheduleResourceService;

	/** The project resource service. */
	@Inject
	private ResourceService resourceService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE)
	private InstanceDao<ScheduleInstance> instanceDao;

	/** The converter. */
	@Inject
	private TypeConverter converter;

	/**
	 * Standalone task reassign.
	 *
	 * @param event
	 *            the event
	 */
	public void standaloneTaskReassign(@Observes StandaloneTaskReassignEvent event) {
		String oldUser = event.getOldUser();

		StandaloneTaskInstance taskInstance = event.getInstance();
		String newUser = (String) taskInstance.getProperties().get(TaskProperties.TASK_OWNER);
		updateReassign(taskInstance, newUser, oldUser);
	}

	/**
	 * Task instance reassign event listener. Updates the assignment entry
	 *
	 * @param event
	 *            the event
	 */
	public void taskInstanceReassign(@Observes TaskReassignEvent event) {
		String newUser = (String) event.getInstance().getProperties()
				.get(TaskProperties.TASK_OWNER);
		updateReassign(event.getInstance(), newUser, event.getOldUser());
	}

	/**
	 * Standalone task claim event listener. Updates the assignment entry
	 *
	 * @param event
	 *            the event
	 */
	public void standaloneTaskClaim(@Observes StandaloneTaskClaimEvent event) {
		String newUser = (String) event.getInstance().getProperties()
				.get(TaskProperties.TASK_OWNER);
		updateReassign(event.getInstance(), newUser, null);
	}

	/**
	 * Standalone task release event listener. Updates the assignment entry
	 *
	 * @param event
	 *            the event
	 */
	public void standaloneTaskRelease(@Observes StandaloneTaskReleaseEvent event) {
		String owner = (String) event.getInstance().getProperties().get(TaskProperties.TASK_OWNER);
		updateReassign(event.getInstance(), null, owner);
	}

	/**
	 * Update reassign of task. Could be invoked by several methos as reassign,claim,release and so
	 * on.
	 *
	 * @param taskInstance
	 *            the task instance to update assignments for
	 * @param newUser
	 *            the new user.could be null
	 * @param oldUser
	 *            the old user.could be null
	 */
	private void updateReassign(AbstractTaskInstance taskInstance, String newUser, String oldUser) {

		ScheduleEntry entry = scheduleService.getEntryForActualInstance(taskInstance);
		// if entry does not exists or is newly created we cannot work with it
		if ((entry == null) || (entry.getId() == null)) {
			return;
		}
		Resource oldResource = null;
		if (oldUser != null) {
			oldResource = getOrCreateResource(oldUser, entry);
			if (oldResource == null) {
				// no valid resource to update -- nothing to do more
				return;
			}
		}
		Resource newResource = getOrCreateResource(newUser, entry);

		// no valid resource to update -- nothing to do more
		ScheduleAssignment deleted = null;

		List<ScheduleAssignment> assignments = scheduleResourceService.getAssignments(entry);
		for (ScheduleAssignment scheduleAssignment : assignments) {
			if (oldResource != null) {
				if (scheduleAssignment.getResourceId() == oldResource.getId()) {
					deleted = scheduleAssignment;
					scheduleResourceService.delete(scheduleAssignment);
					break;
				}
			} else {
				scheduleResourceService.delete(scheduleAssignment);
				deleted = scheduleAssignment;
			}
		}
		// CMF-4212
		/*if (deleted == null) {
			return;
		}*/
		if (newResource != null) {
			ScheduleAssignment newAssignment = new ScheduleAssignment();
			newAssignment.setResourceId((String) newResource.getId());
			newAssignment.setScheduleId(entry.getScheduleId());
			newAssignment.setTaskId((Long) entry.getId());
			newAssignment.setUnits(deleted != null ? deleted.getUnits() : 100);
			scheduleResourceService.add(newAssignment);
		}
	}

	/**
	 * Gets the or create resource.
	 *
	 * @param user
	 *            the user
	 * @param entry
	 *            the entry
	 * @return the or create resource
	 */
	private Resource getOrCreateResource(String user, ScheduleEntry entry) {
		Resource resource = resourceService.getResource(user, ResourceType.USER);
		if (resource != null) {
			ScheduleInstance scheduleInstance = instanceDao.loadInstance(entry.getScheduleId(),
					null, false);

			Instance parent = converter.convert(InitializedInstance.class,
					scheduleInstance.getOwningReference()).getInstance();

			ResourceRole role = resourceService.getResourceRole(parent, resource);
			if (role == null) {
				ResourceRole resourceRole = resourceService.assignResource(resource,
						SecurityModel.BaseRoles.CONSUMER, parent);
				resource = resourceRole.getResource();
			}
		}
		return resource;
	}
}
