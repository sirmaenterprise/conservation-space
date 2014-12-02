package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;

/**
 * Event fired when schedule assignment is being deleted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when schedule assignment is being deleted.")
public class ScheduleAssignmentDeletedEvent implements EmfEvent {

	/** The assignment. */
	private final ScheduleAssignment assignment;

	/**
	 * Instantiates a new schedule assignment deleted event.
	 * 
	 * @param assignment
	 *            the assignment
	 */
	public ScheduleAssignmentDeletedEvent(ScheduleAssignment assignment) {
		this.assignment = assignment;
	}

	/**
	 * Gets the dependency.
	 * 
	 * @return the dependency
	 */
	public ScheduleAssignment getDependency() {
		return assignment;
	}
}
