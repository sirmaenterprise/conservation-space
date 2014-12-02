package com.sirma.itt.pm.schedule.observers;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.task.workflow.AfterTaskPersistEvent;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.ScheduleSynchronizationInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.util.DateUtil;

/**
 * Observer that creates a schedule entries based on a persisted instances.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AutoScheduleUpdate {

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> instanceDao;

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	/** The schedule resource service. */
	@Inject
	private ScheduleResourceService scheduleResourceService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/**
	 * On new instance persisted.
	 *
	 * @param event
	 *            the event
	 */
	public void onNewInstancePersisted(
			@Observes AfterInstancePersistEvent<Instance, TwoPhaseEvent> event) {
		Instance instance = event.getInstance();
		if (!(instance instanceof ScheduleSynchronizationInstance)) {
			return;
		}
		Instance rootInstance = InstanceUtil.getRootInstance(instance, true);
		// if the object is not a part of a project we does not need to do anything
		if (!(rootInstance instanceof ProjectInstance)) {
			return;
		}

		if (AfterTaskPersistEvent.class.isInstance(event)) {
			Instance previousTask = AfterTaskPersistEvent.class.cast(event).getPreviousTask();
			if (previousTask != null) {
				createScheduleEntry(previousTask);
			}
		} else {
			createScheduleEntry(rootInstance);
		}
		createScheduleEntry(instance);
	}

	/**
	 * Creates the schedule entry.
	 *
	 * @param instance
	 *            the instance
	 * @return the schedule entry
	 */
	private ScheduleEntry createScheduleEntry(Instance instance) {
		ScheduleEntry scheduleEntry = scheduleService.getEntryForActualInstance(instance);
		if ((scheduleEntry == null) || (scheduleEntry.getId() != null)) {
			return scheduleEntry;
		}

		Instance directParent = InstanceUtil.getDirectParent(instance, true);
		if (directParent == null) {
			if (instance instanceof TaskInstance) {
				directParent = ((TaskInstance) instance).getContext();
			} else {
				return null;
			}
		}
		ScheduleEntry parent = null;
		if (directParent instanceof ProjectInstance) {
			// ensure we have created schedule and schedule entry
			instanceService.refresh(directParent);
			ScheduleInstance schedule = scheduleService.getSchedule(directParent);
			if (schedule == null) {
				final Instance directLocal = directParent;
				schedule = dbDao.invokeInNewTx(new Callable<ScheduleInstance>() {

					@Override
					public ScheduleInstance call() throws Exception {
						return scheduleService.getOrCreateSchedule(directLocal);
					}
				});
			}
			parent = scheduleService.getEntryForActualInstance(directParent);
		} else {
			parent = createScheduleEntry(directParent);
			if (parent == null) {
				// add logging
				return null;
			}
		}

		Long scheduleId = parent.getScheduleId();

		List<ScheduleEntry> children = scheduleService.getChildren(parent.getId());
		// Calculate index for the new entry to keep scheduler in order
		int maxIndex = -1;
		for (ScheduleEntry child : children) {
			Serializable index = child.getProperties().get(ScheduleEntryProperties.INDEX);
			if (index != null) {
				try {
					int childIndex = Integer.valueOf(String.valueOf(index));
					if (childIndex >= maxIndex) {
						maxIndex = childIndex;
					}
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		scheduleEntry.getProperties().put(ScheduleEntryProperties.INDEX, String.valueOf(maxIndex+1));

		// update the schedule entry and save it
		scheduleEntry.setScheduleId(scheduleId);
		scheduleEntry.setParentId(parent.getId());
		// TODO may be the #startDate and #endDate
		Date start = (Date) scheduleEntry.getProperties().get(
				ScheduleEntryProperties.PLANNED_START_DATE);
		Date end = (Date) scheduleEntry.getProperties().get(
				ScheduleEntryProperties.PLANNED_END_DATE);
		if ((start != null) && (end != null)) {
			Double duration = DateUtil.daysBetween(start, end, true);
			if (duration.longValue() == 0) {
				duration = 1.0;
			}
			scheduleEntry.getProperties().put(ScheduleEntryProperties.DURATION, duration);
		} else {
			scheduleEntry.getProperties().put(ScheduleEntryProperties.DURATION, 1.0);
		}

		// remove properties that are not in the current instance so we can perform an ADD operation
		// to the properties and not UPDATE operation
		scheduleEntry.getProperties().keySet().removeAll(instance.getProperties().keySet());
		List<ScheduleEntry> list = scheduleService.addEntry(scheduleEntry);
		ScheduleEntry entry = list.get(0);

		String assignedResource = null;

		// get the correct assignment
		if (instance instanceof AbstractTaskInstance) {
			assignedResource = (String) instance.getProperties().get(TaskProperties.TASK_OWNER);
		} else {
			assignedResource = (String) instance.getProperties().get(DefaultProperties.CREATED_BY);
		}
		if (assignedResource != null) {
			Resource resource = resourceService.getResource(assignedResource, ResourceType.USER);
			ScheduleAssignment assignment = new ScheduleAssignment();
			assignment.setResourceId((String) resource.getId());
			assignment.setScheduleId(scheduleId);
			assignment.setTaskId((Long) entry.getId());
			assignment.setUnits(100);
			scheduleResourceService.add(assignment);
		}
		return entry;
	}
}
