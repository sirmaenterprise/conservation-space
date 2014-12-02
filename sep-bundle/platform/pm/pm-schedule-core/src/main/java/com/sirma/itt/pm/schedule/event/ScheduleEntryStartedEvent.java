package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired when {@link ScheduleEntry} is started.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link ScheduleEntry} is started.")
public class ScheduleEntryStartedEvent extends AbstractInstanceEvent<ScheduleEntry> {

	/**
	 * Instantiates a new schedule entry started event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ScheduleEntryStartedEvent(ScheduleEntry instance) {
		super(instance);
	}
}
