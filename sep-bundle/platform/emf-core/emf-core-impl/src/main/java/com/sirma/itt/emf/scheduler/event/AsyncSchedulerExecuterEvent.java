package com.sirma.itt.emf.scheduler.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.scheduler.SchedulerEntry;
import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.util.Documentation;

/**
 * System event that is fired to trigger asynchronous action execution.
 */
@Documentation("System event that is fired to trigger asynchronous action execution.")
public class AsyncSchedulerExecuterEvent implements EmfEvent {

	/** The security context. */
	private SecurityContext securityContext;

	/** The scheduler entry. */
	private SchedulerEntry schedulerEntry;

	/**
	 * Instantiates a new async scheduler executer event.
	 * 
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @param securityContext
	 *            the security context
	 */
	public AsyncSchedulerExecuterEvent(SchedulerEntry schedulerEntry,
			SecurityContext securityContext) {
		this.schedulerEntry = schedulerEntry;
		this.securityContext = securityContext;
	}

	/**
	 * Getter method for securityContext.
	 * 
	 * @return the securityContext
	 */
	public SecurityContext getSecurityContext() {
		return securityContext;
	}

	/**
	 * Setter method for securityContext.
	 * 
	 * @param securityContext
	 *            the securityContext to set
	 */
	public void setSecurityContext(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}

	/**
	 * Getter method for schedulerEntry.
	 *
	 * @return the schedulerEntry
	 */
	public SchedulerEntry getSchedulerEntry() {
		return schedulerEntry;
	}

	/**
	 * Setter method for schedulerEntry.
	 *
	 * @param schedulerEntry the schedulerEntry to set
	 */
	public void setSchedulerEntry(SchedulerEntry schedulerEntry) {
		this.schedulerEntry = schedulerEntry;
	}
}