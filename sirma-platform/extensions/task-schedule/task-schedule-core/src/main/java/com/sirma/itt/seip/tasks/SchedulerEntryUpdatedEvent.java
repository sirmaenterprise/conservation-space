package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;
import com.sirma.itt.seip.security.util.SecureExecutor;
import com.sirma.itt.seip.tasks.SchedulerEntry;

/**
 * Event fired when scheduler entry has been updated.
 *
 * @author BBonev
 */
@Documentation("Event fired when scheduler entry has been updated.")
public class SchedulerEntryUpdatedEvent extends AbstractSecureEvent {

	/** The entry. */
	private final SchedulerEntry newEntry;
	/** The old entry. */
	private final SchedulerEntry oldEntry;

	/**
	 * Instantiates a new scheduler entry updated event.
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
	 * Instantiates a new scheduler entry added event.
	 *
	 * @param newEntry
	 *            the new entry
	 * @param oldEntry
	 *            the old entry
	 * @param secureExecutor
	 *            the secure executor
	 */
	public SchedulerEntryUpdatedEvent(SchedulerEntry newEntry, SchedulerEntry oldEntry, SecureExecutor secureExecutor) {
		this.newEntry = newEntry;
		this.oldEntry = oldEntry;
		setSecureExecutor(secureExecutor);
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
