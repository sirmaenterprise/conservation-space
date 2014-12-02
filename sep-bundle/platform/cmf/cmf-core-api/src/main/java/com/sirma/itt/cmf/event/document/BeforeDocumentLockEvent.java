package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document to be locked by a particular user.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document to be locked by a particular user.")
public class BeforeDocumentLockEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentLockEvent> {

	/** The locked by. */
	private final String lockedBy;

	/**
	 * Instantiates a new before document lock event.
	 * 
	 * @param instance
	 *            the instance
	 * @param lockedBy
	 *            the locked by
	 */
	public BeforeDocumentLockEvent(DocumentInstance instance, String lockedBy) {
		super(instance);
		this.lockedBy = lockedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentLockEvent createNextEvent() {
		return new AfterDocumentLockEvent(getInstance(), getLockedBy());
	}

	/**
	 * Getter method for lockedBy.
	 * 
	 * @return the lockedBy
	 */
	public String getLockedBy() {
		return lockedBy;
	}

}
