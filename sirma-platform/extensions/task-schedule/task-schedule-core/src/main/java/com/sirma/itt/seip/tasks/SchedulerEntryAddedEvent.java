package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;
import com.sirma.itt.seip.security.util.SecureExecutor;
import com.sirma.itt.seip.tasks.SchedulerEntry;

/**
 * Event fired when we scheduler entry has been added.
 *
 * @author BBonev
 */
@Documentation("Event fired when we scheduler entry has been added")
public class SchedulerEntryAddedEvent extends AbstractSecureEvent {

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
	 * Instantiates a new scheduler entry added event.
	 *
	 * @param entry
	 *            the entry
	 * @param secureExecutor
	 *            the secure executor
	 */
	public SchedulerEntryAddedEvent(SchedulerEntry entry, SecureExecutor secureExecutor) {
		this.entry = entry;
		setSecureExecutor(secureExecutor);
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
