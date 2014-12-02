package com.sirma.itt.cmf.event.task.worklog;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when user logs work on a task. The event will carry all passed arguments to the
 * method.
 * 
 * @author BBonev
 */
@Documentation("Event fired when user logs work on a task. The event will carry all passed arguments to the method.")
public class AddedWorkLogEntryEvent implements EmfEvent {

	/** The user id. */
	private final String userId;

	/** The properties. */
	private final Map<String, Serializable> properties;

	/** The logged entry id. */
	private final Serializable loggedEntryId;

	/** The instance reference. */
	private final InstanceReference instanceRef;

	/**
	 * Instantiates a new work logged event.
	 * 
	 * @param instanceRef
	 *            the instance reference
	 * @param loggedEntryId
	 *            the id of the created logged work entry.
	 * @param userId
	 *            the user id
	 * @param properties
	 *            the properties
	 */
	public AddedWorkLogEntryEvent(InstanceReference instanceRef, Serializable loggedEntryId,
			String userId, Map<String, Serializable> properties) {
		this.instanceRef = instanceRef;
		this.userId = userId;
		this.properties = properties;
		this.loggedEntryId = loggedEntryId;
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
	 * Getter method for userId.
	 * 
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Getter method for properties.
	 *
	 * @return the properties
	 */
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * Getter method for loggedEntryId.
	 * 
	 * @return the loggedEntryId
	 */
	public Serializable getLoggedEntryId() {
		return loggedEntryId;
	}

}
