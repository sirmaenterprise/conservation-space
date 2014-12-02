package com.sirma.itt.pm.schedule.observers;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.pm.schedule.event.ScheduleEntryDeletedEvent;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;

/**
 * Observer that listens for schedule entry deletion. Currently the observer removes assignment and
 * resources associated with the entry.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class OnDeletedScheduleEntry {

	/** The resource service. */
	@Inject
	private ScheduleResourceService resourceService;

	/**
	 * Entry deleted.
	 * 
	 * @param event
	 *            the event
	 */
	public void entryDeleted(@Observes ScheduleEntryDeletedEvent event) {
		ScheduleEntry entry = event.getInstance();

		List<ScheduleAssignment> assignments = resourceService.getAssignments(entry);
		List<ScheduleDependency> dependencies = resourceService.getDependencies(entry, null);

		for (ScheduleAssignment scheduleAssignment : assignments) {
			resourceService.delete(scheduleAssignment);
		}
		for (ScheduleDependency scheduleDependency : dependencies) {
			resourceService.delete(scheduleDependency);
		}
	}

}
