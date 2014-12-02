package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;

/**
 * Event fired when new {@link ScheduleAssignment} is added for a task entry.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new {@link ScheduleAssignment} is added for a task entry.")
public class ScheduleAssignmentAddedEvent implements EmfEvent {

	/** The assignment. */
	private final ScheduleAssignment assignment;

	/**
	 * Instantiates a new schedule assignment added event.
	 * 
	 * @param assignment
	 *            the assignment
	 */
	public ScheduleAssignmentAddedEvent(ScheduleAssignment assignment) {
		this.assignment = assignment;
	}

	/**
	 * Gets the assignment.
	 * 
	 * @return the assignment
	 */
	public ScheduleAssignment getAssignment() {
		return assignment;
	}

}
