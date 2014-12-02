package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired when {@link ScheduleEntry} is added and persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link ScheduleEntry} is added and persisted for the first time.")
public class ScheduleEntryAddedEvent extends AbstractInstanceEvent<ScheduleEntry> {

	/**
	 * Instantiates a new schedule entry added event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ScheduleEntryAddedEvent(ScheduleEntry instance) {
		super(instance);
	}
}
