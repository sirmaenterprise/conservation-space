package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document check out from DMS.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document check out from DMS")
public class BeforeDocumentCheckOutEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentCheckOutEvent> {

	/** The user id. */
	private final String userId;

	/**
	 * Instantiates a new before document check out event.
	 * 
	 * @param instance
	 *            the instance
	 * @param userId
	 *            the user id
	 */
	public BeforeDocumentCheckOutEvent(DocumentInstance instance, String userId) {
		super(instance);
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentCheckOutEvent createNextEvent() {
		return new AfterDocumentCheckOutEvent(getInstance(), getUserId());
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
