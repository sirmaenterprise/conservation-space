package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired on {@link ScheduleEntry} cancellation.
 * 
 * @author BBonev
 */
@Documentation("Event fired on {@link ScheduleEntry} cancellation.")
public class ScheduleEntryCanceledEvent extends AbstractInstanceEvent<ScheduleEntry> {

	/**
	 * Instantiates a new schedule entry canceled event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ScheduleEntryCanceledEvent(ScheduleEntry instance) {
		super(instance);
	}

}
