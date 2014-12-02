package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document check in in DMS.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document check in in DMS")
public class BeforeDocumentCheckInEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentCheckInEvent> {

	/** The user id. */
	private final String userId;

	/**
	 * Instantiates a new before document check in event.
	 * 
	 * @param instance
	 *            the instance
	 * @param userId
	 *            the user id
	 */
	public BeforeDocumentCheckInEvent(DocumentInstance instance, String userId) {
		super(instance);
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentCheckInEvent createNextEvent() {
		return new AfterDocumentCheckInEvent(getInstance(), getUserId());
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
