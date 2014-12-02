package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.instance.InstanceMovedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired when schedule entry is being moved in the schedule and notifies that there is a
 * parent change. The parents are other schedule entries.
 * 
 * @author BBonev
 */
@Documentation("Event fired when schedule entry is being moved in the schedule and notifies that there is a parent change. The parents are other schedule entries.")
public class ScheduleEntryMovedEvent extends InstanceMovedEvent<ScheduleEntry> {

	/**
	 * Instantiates a new schedule entry moved event.
	 * 
	 * @param instance
	 *            the instance
	 * @param oldParent
	 *            the old parent
	 * @param newParent
	 *            the new parent
	 */
	public ScheduleEntryMovedEvent(ScheduleEntry instance, Instance oldParent, Instance newParent) {
		super(instance, oldParent, newParent);
	}

}
