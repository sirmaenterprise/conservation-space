/*
 *
 */
package com.sirma.itt.pm.schedule.observers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntry;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.event.StateChangedEvent;
import com.sirma.itt.pm.schedule.constants.ScheduleConfigProperties;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.event.ScheduleDependencyAddedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleDependencyDeletedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryAddedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryDeletedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryStartedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryUpdatedEvent;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.schedulerActions.StartScheduleEntryAction;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.util.DateUtil;

/**
 * The implementation monitors for events that are relevant for synchronizing the automatic actions
 * for the schedule.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SchedulerActionsSynchronizationObserver {

	/**
	 * The time between 2 tasks that are dependent on from another so we will ensure the parent
	 * tasks are started and flushed to DB before attempting to start the next one.
	 */
	private static final int CHAINED_TASKS_DELAY = 10 * 1000;

	/** The Constant TIMED_START. */
	private static final String TIMED_START = "TIMED_START";

	/** The auto mode. */
	@Inject
	@Config(name = ScheduleConfigProperties.SCHEDULE_START_MODE_AUTO, defaultValue = "Auto")
	private String autoMode;
	/** The manual mode. */
	@Inject
	@Config(name = ScheduleConfigProperties.SCHEDULE_START_MODE_MANUAL, defaultValue = "Manual")
	private String manualMode;

	/** The allow tasks in the past. */
	@Inject
	@Config(name = ScheduleConfigProperties.SCHEDULE_ALLOW_AUTOSTARTING_OF_TASKS_IN_THE_PAST, defaultValue = "false")
	private Boolean allowTasksInThePast;

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;
	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;
	/** The schedule entry dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> scheduleEntryDao;
	/** The resource service. */
	@Inject
	private ScheduleResourceService resourceService;
	/** The state service. */
	@Inject
	private StateService stateService;
	/** The logger. */
	@Inject
	private Logger logger;

	/**
	 * Listens for instance state change and schedules schedule entries to be started
	 *
	 * @param event
	 *            the event
	 */
	public void onStateChange(@Observes StateChangedEvent event) {
		String newState = event.getNewState();
		String oldState = event.getOldState();
		// TODO: what about cancel
		Instance instance = event.getInstance();
		// task is completed successfully
		if (stateService.isState(PrimaryStates.COMPLETED, instance.getClass(), newState)) {
			scheduleDependenciesForInstance(instance, ScheduleDependency.TYPE_END_TO_START);
		} else if (stateService.isState(PrimaryStates.INITIAL, instance.getClass(), oldState)) {
			// task was started out of the schedule
			scheduleDependenciesForInstance(instance, ScheduleDependency.TYPE_START_TO_START);
		}
	}

	/**
	 * On schedule entry started from schedule or via trigger.
	 *
	 * @param event
	 *            the event
	 */
	public void onEntryStarted(@Observes ScheduleEntryStartedEvent event) {
		ScheduleEntry entry = event.getInstance();
		scheduleDependencies(entry, ScheduleDependency.TYPE_START_TO_START);
	}

	/**
	 * Schedules any entries that have the given dependency type for execution.
	 *
	 * @param instance
	 *            the instance
	 * @param dependencyType
	 *            the dependency type
	 */
	private void scheduleDependenciesForInstance(Instance instance, int dependencyType) {
		// check if we have a project at the root
		if (InstanceUtil.getRootInstance(instance, true) instanceof RootInstanceContext) {
			// method that does not create new entry all the time
			ScheduleEntry scheduleEntry = scheduleService.getEntryForActualInstance(instance, false);
			if (scheduleEntry == null) {
				// there is no schedule entry for the given instance
				return;
			}
			scheduleDependencies(scheduleEntry, dependencyType);
		}
	}

	/**
	 * Schedule dependencies for the given entry of the given type.
	 *
	 * @param scheduleEntry
	 *            the schedule entry
	 * @param dependencyType
	 *            the dependency type
	 */
	private void scheduleDependencies(ScheduleEntry scheduleEntry, int dependencyType) {
		List<ScheduleDependency> dependencies = resourceService.getDependencies(scheduleEntry,
				Boolean.TRUE);
		if (dependencies.isEmpty()) {
			// nothing to trigger
			return;
		}

		Map<Long, ScheduleDependency> entriesToLoad = new LinkedHashMap<Long, ScheduleDependency>();
		for (ScheduleDependency dependency : dependencies) {
			if (dependency.getType().intValue() == dependencyType) {
				entriesToLoad.put(dependency.getTo(), dependency);
			}
		}
		if (entriesToLoad.isEmpty()) {
			// nothing to start
			return;
		}
		scheduleEntriesBasedOnDependencies(entriesToLoad);
	}

	/**
	 * Schedule entries that are connected the via the given dependency list. The mapping is build
	 * from the entry that need to be loaded as {@link ScheduleEntry} and the corresponding
	 * dependency that triggers it.
	 *
	 * @param entriesToLoad
	 *            the entries to load
	 */
	private void scheduleEntriesBasedOnDependencies(Map<Long, ScheduleDependency> entriesToLoad) {
		List<ScheduleEntry> list = scheduleEntryDao
				.loadInstancesByDbKey(new ArrayList<Long>(entriesToLoad.keySet()));
		for (ScheduleEntry scheduleEntry : list) {
			if (!isAutoModeApplicable(scheduleEntry, true)) {
				// the entry has been started or is scheduled for manual start
				continue;
			}
			ScheduleDependency dependency = entriesToLoad.get(scheduleEntry.getId());
			if (dependency == null) {
				continue;
			}
			Date plannedStartDate = getPlannedStartDate(scheduleEntry);
			long offset = DateUtil.convertToMilliseconds(dependency.getLag(),
					dependency.getLagUnit());
			if (offset < CHAINED_TASKS_DELAY) {
				// delay dependent tasks by 10 seconds so the parent if scheduled to be started is
				// started before this entry.
				offset = CHAINED_TASKS_DELAY;
			}
//			startDate = new Date(plannedStartDate.getTime() + offset);
			// Start condition is met. Schedule entry for current time plus lag offset
			Date startDate = new Date(System.currentTimeMillis() + offset);
			// Pick the most recent date
			startDate = startDate.before(plannedStartDate)?startDate:plannedStartDate;
			scheduleTimedEntry(scheduleEntry, startDate);
		}
	}

	/**
	 * Listens for new dependencies and disables the event for timed starting of the dependent
	 * schedule entry :).
	 *
	 * @param event
	 *            the event
	 */
	public void onDependencyAdded(@Observes ScheduleDependencyAddedEvent event) {
		ScheduleDependency dependency = event.getDependency();
		if (dependency.getType() == null) {
			return;
		}
		List<ScheduleEntry> list = scheduleEntryDao
				.loadInstancesByDbKey(Arrays.asList(dependency.getFrom(), dependency.getTo()));
		if (list.size() != 2) {
			// something is wrong here, we have missing tasks
			logger.warn("Searched for 2 schedule entries but found " + list.size());
			return;
		}
		ScheduleEntry from = list.get(0);
		ScheduleEntry to = list.get(1);

		if (!isAutoModeApplicable(to, true)) {
			// not eligible for start
			return;
		}
		if (from.getActualInstanceId() != null) {
			if ((dependency.getType().intValue() == ScheduleDependency.TYPE_END_TO_START)
					|| (dependency.getType().intValue() == ScheduleDependency.TYPE_START_TO_START)) {
				// schedule the entry for direct execution due to the parent has been started
				scheduleTimedEntry(to, null);
			}
		} else {
			if (from.getActualInstanceClass() == null) {
				// we cannot schedule until we have a concrete class
				return;
			}

			String schedulerID = buildSchedulerID(to, TIMED_START);
			SchedulerEntry entry = schedulerService.getScheduleEntry(schedulerID);
			// first we check if we have an entry scheduled by time so we need to cancel it for the
			// time being, because will be rescheduled based on the actual start/end date of of the
			// dependent object
			if (entry != null) {
				entry.setStatus(SchedulerEntryStatus.CANCELED);
				schedulerService.save(entry);
			}
		}
	}

	/**
	 * Listens for dependency deletion and schedules the dependent entry to be started on the
	 * planned time again.
	 *
	 * @param event
	 *            the event
	 */
	public void onDependencyDeleted(@Observes ScheduleDependencyDeletedEvent event) {
		ScheduleDependency dependency = event.getDependency();
		if (dependency.getType() == null) {
			return;
		}
		List<ScheduleEntry> list = scheduleEntryDao
				.loadInstancesByDbKey(Arrays.asList(dependency.getFrom(), dependency.getTo()));
		if (list.size() != 2) {
			// something is wrong here, we have missing tasks
			logger.warn("Searched for 2 schedule entries but found " + list.size());
			return;
		}
		ScheduleEntry from = list.get(0);
		ScheduleEntry to = list.get(1);

		if (!isAutoModeApplicable(to, true)) {
			// not eligible for start
			return;
		}
		if (from.getActualInstanceId() != null) {
			if ((dependency.getType().intValue() == ScheduleDependency.TYPE_END_TO_START)
					|| (dependency.getType().intValue() == ScheduleDependency.TYPE_START_TO_START)) {
				// schedule the entry for direct execution due to the parent has been started
				scheduleTimedEntry(to, null);
			}
		} else {
			if (from.getActualInstanceClass() == null) {
				// we cannot schedule until we have a concrete class
				return;
			}

			String schedulerID = buildSchedulerID(to, TIMED_START);
			SchedulerEntry entry = schedulerService.getScheduleEntry(schedulerID);
			// first we check if we have an entry scheduled by time so we need to activate it,
			// because it was probably canceled on adding the dependency
			if ((entry != null) && (entry.getStatus() == SchedulerEntryStatus.CANCELED)) {
				// update the start date
				SchedulerConfiguration configuration = entry.getConfiguration();
				Date plannedStartDate = getPlannedStartDate(to);
				configuration.setScheduleTime(plannedStartDate);

				entry.setStatus(SchedulerEntryStatus.NOT_RUN);
				schedulerService.save(entry);
			}
		}
	}

	/**
	 * Checks for new schedule entries and if all data is filled a schedule trigger is created for
	 * the entry to be started at a given planned time.
	 *
	 * @param event
	 *            the event
	 */
	public void onScheduleEntryAdded(@Observes ScheduleEntryAddedEvent event) {
		ScheduleEntry scheduleEntry = event.getInstance();
		if (!isAutoModeApplicable(scheduleEntry)) {
			// nothing to do here
			return;
		}

		scheduleTimedEntry(scheduleEntry, null);
	}

	/**
	 * Schedule the given entry to be started at his planned start date or the given start date. The
	 * argument date is with higher priority.
	 *
	 * @param scheduleEntry
	 *            the schedule entry
	 * @param startDate
	 *            the start date
	 */
	private void scheduleTimedEntry(ScheduleEntry scheduleEntry, Date startDate) {
		SchedulerConfiguration configuration;
		SchedulerContext context;
		String identifier = buildSchedulerID(scheduleEntry, TIMED_START);
		SchedulerEntry entry = schedulerService.getScheduleEntry(identifier);
		if (entry != null) {
			configuration = entry.getConfiguration();
			entry.setStatus(SchedulerEntryStatus.NOT_RUN);
			context = entry.getContext();
		} else {
			configuration = schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED);
			configuration.setIdentifier(identifier);
			// If parent and child are scheduled for the same time it may fail to start the child. See CMF-2525
			configuration.setMaxRetryCount(10);
			configuration.setRetryDelay(new Long(CHAINED_TASKS_DELAY));
			context = new SchedulerContext();
		}

		Date plannedStartDate = getPlannedStartDate(scheduleEntry);
		// if we should reschedule
		configuration.setScheduleTime(startDate == null ? plannedStartDate : startDate);
		context.put(StartScheduleEntryAction.ENTRY_ID, scheduleEntry.getId());
		context.put(StartScheduleEntryAction.SCHEDULE_ID, scheduleEntry.getScheduleId());

		if (entry == null) {
			SchedulerEntry schedulerEntry = schedulerService.schedule(
					StartScheduleEntryAction.ACTION_NAME, configuration, context);
			if (schedulerEntry != null) {
				logger.debug("Scheduled schedule entry to be started on " + plannedStartDate
						+ " with ID=" + identifier);
			}
		} else {
			schedulerService.save(entry);
		}
	}

	/**
	 * Builds a scheduler entry identifier that could be reverted if needed.Current format is:<br>
	 * scheduleEntryClass_nameOfTheActualType_eventType=time_entryDbId
	 *
	 * @param scheduleEntry
	 *            the schedule entry to build the identifier from
	 * @param type
	 *            the type of the event
	 * @return the build identifier.
	 */
	private String buildSchedulerID(ScheduleEntry scheduleEntry, String type) {
		Date plandDate = getPlannedStartDate(scheduleEntry);
		return scheduleEntry.getClass().getSimpleName() + "_"
				+ scheduleEntry.getInstanceReference().getReferenceType().getName() + "_" + type
				+ "=" + plandDate.getTime() + "_" + scheduleEntry.getId();
	}

	/**
	 * Returns the schedule entry start planned date.
	 *
	 * @param scheduleEntry
	 *            the schedule entry
	 * @return the planned start date
	 */
	private Date getPlannedStartDate(ScheduleEntry scheduleEntry) {
		return (Date) scheduleEntry.getProperties().get(ScheduleEntryProperties.PLANNED_START_DATE);
	}

	/**
	 * Checks if the given entry has filled all basic requirements to be scheduled for starting. The
	 * method checks for: if the instance is not already started, has defined type, has a planned
	 * start date and that is not is the past, also the start mode of the entry should be automatic.
	 *
	 * @param scheduleEntry
	 *            the schedule entry
	 * @return true, if checks if is auto mode applicable
	 */
	private boolean isAutoModeApplicable(ScheduleEntry scheduleEntry) {
		return isAutoModeApplicable(scheduleEntry, allowTasksInThePast);
	}

	/**
	 * Checks if the given entry has filled all basic requirements to be scheduled for starting. The
	 * method checks for: if the instance is not already started, has defined type, has a planned
	 * start date (required) and that is not is the past (optional check), also the start mode of
	 * the entry should be automatic.
	 *
	 * @param scheduleEntry
	 *            the schedule entry
	 * @param allowInPast
	 *            allow start planned date to be in the past
	 * @return true, if checks if is auto mode applicable
	 */
	private boolean isAutoModeApplicable(ScheduleEntry scheduleEntry, boolean allowInPast) {
		if (scheduleEntry.getActualInstanceId() != null) {
			// entry already started nothing to do
			return false;
		}
		if (scheduleEntry.getActualInstanceClass() == null) {
			// if the type is not set yet, we will not schedule anything
			return false;
		}
		Date plannedStartDate = getPlannedStartDate(scheduleEntry);
		if ((plannedStartDate == null)
				|| (allowInPast && (plannedStartDate.getTime() < System.currentTimeMillis()))) {
			// date already passed nothing to do more
			// by requirement we should not start tasks that are in the past
			return false;
		}

		String startMode = (String) scheduleEntry.getProperties().get(
				ScheduleEntryProperties.START_MODE);
		if (manualMode.equalsIgnoreCase(startMode)) {
			// not intended for automatic start
			return false;
		}
		return autoMode.equalsIgnoreCase(startMode);
	}

	/**
	 * Listens for update events for schedule entry and checks if something has been changed for the
	 * entry that is revelant to the automatic starting of the entry.
	 *
	 * @param event
	 *            the event
	 */
	public void onScheduleEntryUpdated(@Observes ScheduleEntryUpdatedEvent event) {
		ScheduleEntry oldEntry = event.getOldEntry();
		ScheduleEntry newEntry = event.getNewEntry();

		// is the new entry OK for scheduling
		if (!isAutoModeApplicable(newEntry)) {
			// not yet, nothing to do here
			if (isAutoModeApplicable(oldEntry)) {
				// we need to cancel the old entry
				String oldSchedulerID = buildSchedulerID(oldEntry, TIMED_START);
				SchedulerEntry entry = schedulerService.getScheduleEntry(oldSchedulerID);
				if (entry != null) {
					entry.setStatus(SchedulerEntryStatus.CANCELED);
					schedulerService.save(entry);
				}
			}
			return;
		}
		// if the old entry was OK for schedule
		if (isAutoModeApplicable(oldEntry)) {
			// lets check if we have some changes
			String oldSchedulerID = buildSchedulerID(oldEntry, TIMED_START);
			SchedulerEntry entry = schedulerService.getScheduleEntry(oldSchedulerID);
			if (entry != null) {
				String newSchedulerID = buildSchedulerID(newEntry, TIMED_START);
				if (newSchedulerID.equals(oldSchedulerID)) {
					// the time and the type of the instance has not been changed so nothing to update
					return;
				}
				// found changes in type or start time
				// update the scheduler ID
				entry.setIdentifier(newSchedulerID);
				// we have already scheduled the entity to start, lets check for changes
				SchedulerConfiguration configuration = entry.getConfiguration();
				configuration.setScheduleTime(getPlannedStartDate(newEntry));

				// reset the status
				if (entry.getStatus() == SchedulerEntryStatus.CANCELED) {
					entry.setStatus(SchedulerEntryStatus.NOT_RUN);
				}
				schedulerService.save(entry);
			}
		} else {
			scheduleTimedEntry(newEntry, null);
		}
	}

	/**
	 * Listens for delete events to cancel automatic starting.
	 *
	 * @param event
	 *            the event
	 */
	public void onScheduleEntryDeleted(@Observes ScheduleEntryDeletedEvent event) {
		ScheduleEntry scheduleEntry = event.getInstance();
		if (isAutoModeApplicable(scheduleEntry, true)) {
			String schedulerID = buildSchedulerID(scheduleEntry, TIMED_START);
			SchedulerEntry entry = schedulerService.getScheduleEntry(schedulerID);
			if (entry != null) {
				entry.setStatus(SchedulerEntryStatus.CANCELED);
				entry.getConfiguration().setScheduleTime(null);
				schedulerService.save(entry);
			}
		}
	}
}
