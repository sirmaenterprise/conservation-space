package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document has been checked in into DMS.
 * 
 * @author BBonev
 */
@Documentation("Event fired after document has been checked in into DMS")
public class AfterDocumentCheckInEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/** The user id. */
	private final String userId;

	/**
	 * Instantiates a new after document check in event.
	 * 
	 * @param instance
	 *            the instance
	 * @param userId
	 *            the user id
	 */
	public AfterDocumentCheckInEvent(DocumentInstance instance, String userId) {
		super(instance);
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	/**
	 * Getter method for userId.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

}
