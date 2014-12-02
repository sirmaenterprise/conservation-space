package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired when schedule entry has been deleted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when schedule entry has been deleted.")
public class ScheduleEntryDeletedEvent extends AbstractInstanceEvent<ScheduleEntry> {

	/**
	 * Instantiates a new schedule entry deleted event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ScheduleEntryDeletedEvent(ScheduleEntry instance) {
		super(instance);
	}

}
