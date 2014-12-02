package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;

/**
 * Event fired when schedule assignment is being updated. The most cases this is when the assignment
 * percent is changed.
 * 
 * @author BBonev
 */
@Documentation("Event fired when schedule assignment is being updated. The most cases this is when the assignment percent is changed.")
public class ScheduleAssignmentUpdatedEvent implements EmfEvent {

	/** The old assignment. */
	private final ScheduleAssignment oldAssignment;
	/** The new assignment. */
	private final ScheduleAssignment newAssignment;

	/**
	 * Instantiates a new schedule assignment updated event.
	 * 
	 * @param oldAssignment
	 *            the old assignment
	 * @param newAssignment
	 *            the new assignment
	 */
	public ScheduleAssignmentUpdatedEvent(ScheduleAssignment oldAssignment,
			ScheduleAssignment newAssignment) {
		this.oldAssignment = oldAssignment;
		this.newAssignment = newAssignment;
	}

	/**
	 * Gets the old assignment.
	 * 
	 * @return the old assignment
	 */
	public ScheduleAssignment getOldAssignment() {
		return oldAssignment;
	}

	/**
	 * Gets the new assignment.
	 * 
	 * @return the new assignment
	 */
	public ScheduleAssignment getNewAssignment() {
		return newAssignment;
	}

}
