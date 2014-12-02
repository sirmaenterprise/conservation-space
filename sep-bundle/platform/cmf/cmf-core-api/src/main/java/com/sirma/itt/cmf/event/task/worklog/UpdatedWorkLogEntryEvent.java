package com.sirma.itt.cmf.event.task.worklog;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when an user updates a work log entry in the system. The event will carry the old and
 * the new data also with the information about the user that does the change and the original user
 * logged the work.
 * 
 * @author BBonev
 */
@Documentation("Event fired when an user updates a work log entry in the system. The event will carry "
		+ "the old and the new data also with the information about the user that does the change and "
		+ "the original user logged the work.")
public class UpdatedWorkLogEntryEvent implements EmfEvent {

	/** The updated entry id. */
	private final Serializable updatedEntryId;

	/** The new logged data. */
	private final Map<String, Serializable> newLoggedData;

	/** The old logged data. */
	private final Map<String, Serializable> oldLoggedData;

	/** The user id. */
	private final String updatingUserId;

	/** The original user id. */
	private final String originalUserId;

	/** The instance reference. */
	private final InstanceReference instanceRef;

	/**
	 * Instantiates a new updated work log entry event.
	 * 
	 * @param instanceRef
	 *            the instance reference
	 * @param updatedEntryId
	 *            the updated entry id
	 * @param updatingUserId
	 *            the updating user id
	 * @param originalUserId
	 *            the original user id
	 * @param newLoggedData
	 *            the new logged data
	 * @param oldLoggedData
	 *            the old logged data
	 */
	public UpdatedWorkLogEntryEvent(InstanceReference instanceRef, Serializable updatedEntryId,
			String updatingUserId, String originalUserId, Map<String, Serializable> newLoggedData,
			Map<String, Serializable> oldLoggedData) {
		this.instanceRef = instanceRef;
		this.updatedEntryId = updatedEntryId;
		this.updatingUserId = updatingUserId;
		this.originalUserId = originalUserId;
		this.newLoggedData = newLoggedData;
		this.oldLoggedData = oldLoggedData;
	}

	/**
	 * Getter method for instanceRef.
	 * 
	 * @return the instanceRef
	 */
	public InstanceReference getInstanceRef() {
		return instanceRef;
	}

	/**
	 * Getter method for updatedEntryId.
	 * 
	 * @return the updatedEntryId
	 */
	public Serializable getUpdatedEntryId() {
		return updatedEntryId;
	}

	/**
	 * Getter method for newLoggedData.
	 * 
	 * @return the newLoggedData
	 */
	public Map<String, Serializable> getNewLoggedData() {
		return newLoggedData;
	}

	/**
	 * Getter method for oldLoggedData.
	 * 
	 * @return the oldLoggedData
	 */
	public Map<String, Serializable> getOldLoggedData() {
		return oldLoggedData;
	}

	/**
	 * Getter method for updatingUserId.
	 * 
	 * @return the updatingUserId
	 */
	public String getUpdatingUserId() {
		return updatingUserId;
	}

	/**
	 * Getter method for originalUserId.
	 * 
	 * @return the originalUserId
	 */
	public String getOriginalUserId() {
		return originalUserId;
	}
}
