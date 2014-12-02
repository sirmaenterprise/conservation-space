package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Event fired on change of {@link ScheduleEntry}.
 * 
 * @author BBonev
 */
@Documentation("Event fired on change of {@link ScheduleEntry}.")
public class ScheduleEntryUpdatedEvent implements EmfEvent {

	/** The old entry. */
	private final ScheduleEntry oldEntry;
	/** The new entry. */
	private final ScheduleEntry newEntry;

	/**
	 * Instantiates a new schedule entry updated event.
	 * 
	 * @param oldEntry
	 *            the old entry
	 * @param newEntry
	 *            the new entry
	 */
	public ScheduleEntryUpdatedEvent(ScheduleEntry oldEntry, ScheduleEntry newEntry) {
		this.oldEntry = oldEntry;
		this.newEntry = newEntry;
	}

	/**
	 * Getter method for oldEntry.
	 * 
	 * @return the oldEntry
	 */
	public ScheduleEntry getOldEntry() {
		return oldEntry;
	}

	/**
	 * Getter method for newEntry.
	 * 
	 * @return the newEntry
	 */
	public ScheduleEntry getNewEntry() {
		return newEntry;
	}
}
