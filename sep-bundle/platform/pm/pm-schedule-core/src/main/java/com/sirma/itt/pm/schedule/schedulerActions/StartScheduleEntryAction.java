package com.sirma.itt.pm.schedule.schedulerActions;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;

/**
 * Scheduler action that is responsible for starting scheduler entries
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(StartScheduleEntryAction.ACTION_NAME)
public class StartScheduleEntryAction extends SchedulerActionAdapter {

	/** The Constant NAME. */
	public static final String ACTION_NAME = "startEntryAction";

	/** Required argument that identifies the entry that should be started. */
	public static final String ENTRY_ID = "scheduleEntryId";

	/** Required argument that identifies the schedule instance identifier */
	public static final String SCHEDULE_ID = "scheduleId";

	/** The Constant ARGUMENTS. */
	private static final List<Pair<String, Class<?>>> ARGUMENTS = Arrays.asList(
			new Pair<String, Class<?>>(ENTRY_ID, Long.class), new Pair<String, Class<?>>(
					SCHEDULE_ID, Long.class));

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE)
	private InstanceDao<ScheduleInstance> instanceDao;
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> entryInstanceDao;

	/** The schedule service. */
	@Inject
	private ScheduleService scheduleService;

	@Inject
	private ScheduleResourceService resourceService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(SchedulerContext context) throws Exception {
		Long scheduleEntryId = (Long) context.get(ENTRY_ID);
		Long scheduleId = (Long) context.get(SCHEDULE_ID);

		ScheduleEntry scheduleEntry = entryInstanceDao.loadInstance(scheduleEntryId, null, true);
		if (scheduleEntry == null) {
			// probably the entry was deleted
			return;
		}
		if (scheduleEntry.getActualInstanceId() != null) {
			// the entity was started manually
			return;
		}
		// check if the parent is present
		if (scheduleEntry.getParentId() != null) {
			ScheduleEntry parentEntry = entryInstanceDao.loadInstance(scheduleEntry.getParentId(), null, false);
			if ((parentEntry == null) || (parentEntry.getActualInstanceClass() == null)
					|| (parentEntry.getActualInstanceId() == null)) {
				throw new EmfRuntimeException(
						"Parent instance not started, cannot start the current instance with ID="
								+ scheduleEntryId);
			}
		}

		List<ScheduleAssignment> assignments = resourceService.getAssignments(scheduleEntry);
		if (assignments.isEmpty()) {
			throw new EmfRuntimeException(
					"No assigned users specified, cannot start the current instance with ID="
							+ scheduleEntryId);
		}

		List<ScheduleEntry> entries = Arrays.asList(scheduleEntry);
		ScheduleInstance scheduleInstance = instanceDao.loadInstance(scheduleId, null, false);
		if (scheduleInstance == null) {
			// probably the whole project and entry got deleted
			return;
		}
		List<ScheduleEntry> startedEntries = scheduleService.startEntries(scheduleInstance, entries);
		if (startedEntries.isEmpty()) {
			throw new EmfRuntimeException("Failed to start entry. Check log for errors");
		}
	}

}
