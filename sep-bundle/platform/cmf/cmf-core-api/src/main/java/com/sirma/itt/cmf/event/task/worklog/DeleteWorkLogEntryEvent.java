package com.sirma.itt.cmf.event.task.worklog;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after a work log entry has been deleted successfully. The event will carry all the
 * information available upon deletion.
 * 
 * @author BBonev
 */
@Documentation("Event fired after a work log entry has been deleted successfully. The event will carry all the information available upon deletion.")
public class DeleteWorkLogEntryEvent implements EmfEvent {

	/** The logged entry id. */
	private final Serializable loggedEntryId;

	/** The user id. */
	private final String userId;

	/** The work log data. */
	private final Map<String, Serializable> workLogData;

	/** The instance reference. */
	private final InstanceReference instanceRef;

	/**
	 * Instantiates a new delete work log entry event.
	 * 
	 * @param instanceRef
	 *            the instance ref
	 * @param loggedEntryId
	 *            the logged entry id
	 * @param userId
	 *            the user id
	 * @param workLogData
	 *            the work log data
	 */
	public DeleteWorkLogEntryEvent(InstanceReference instanceRef, Serializable loggedEntryId,
			String userId, Map<String, Serializable> workLogData) {
		this.instanceRef = instanceRef;
		this.loggedEntryId = loggedEntryId;
		this.userId = userId;
		this.workLogData = workLogData;
	}

	/**
	 * Gets the instance reference.
	 * 
	 * @return the instance reference
	 */
	public InstanceReference getInstanceRef() {
		return instanceRef;
	}

	/**
	 * Getter method for loggedEntryId.
	 * 
	 * @return the loggedEntryId
	 */
	public Serializable getLoggedEntryId() {
		return loggedEntryId;
	}

	/**
	 * Getter method for userId.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Getter method for workLogData.
	 *
	 * @return the workLogData
	 */
	public Map<String, Serializable> getWorkLogData() {
		return workLogData;
	}
}
