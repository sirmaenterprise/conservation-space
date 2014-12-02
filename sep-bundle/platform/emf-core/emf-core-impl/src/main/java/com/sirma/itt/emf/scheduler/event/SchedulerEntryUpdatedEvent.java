package com.sirma.itt.emf.scheduler.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.scheduler.SchedulerEntry;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when scheduler entry has been updated.
 * 
 * @author BBonev
 */
@Documentation("Event fired when scheduler entry has been updated.")
public class SchedulerEntryUpdatedEvent implements EmfEvent {

	/** The entry. */
	private final SchedulerEntry newEntry;
	/** The old entry. */
	private final SchedulerEntry oldEntry;

	/**
	 * Instantiates a new scheduler entry added event.
	 * 
	 * @param newEntry
	 *            the new entry
	 * @param oldEntry
	 *            the old entry
	 */
	public SchedulerEntryUpdatedEvent(SchedulerEntry newEntry, SchedulerEntry oldEntry) {
		this.newEntry = newEntry;
		this.oldEntry = oldEntry;
	}

	/**
	 * Getter method for entry.
	 * 
	 * @return the entry
	 */
	public SchedulerEntry getNewEntry() {
		return newEntry;
	}

	/**
	 * Getter method for oldEntry.
	 * 
	 * @return the oldEntry
	 */
	public SchedulerEntry getOldEntry() {
		return oldEntry;
	}
}
