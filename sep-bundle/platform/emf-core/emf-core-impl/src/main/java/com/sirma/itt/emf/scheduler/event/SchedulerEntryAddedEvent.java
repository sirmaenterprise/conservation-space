package com.sirma.itt.emf.scheduler.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.scheduler.SchedulerEntry;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when we scheduler entry has been added.
 * 
 * @author BBonev
 */
@Documentation("Event fired when we scheduler entry has been added")
public class SchedulerEntryAddedEvent implements EmfEvent {

	/** The entry. */
	private final SchedulerEntry entry;

	/**
	 * Instantiates a new scheduler entry added event.
	 * 
	 * @param entry
	 *            the entry
	 */
	public SchedulerEntryAddedEvent(SchedulerEntry entry) {
		this.entry = entry;
	}

	/**
	 * Getter method for entry.
	 * 
	 * @return the entry
	 */
	public SchedulerEntry getEntry() {
		return entry;
	}
}
