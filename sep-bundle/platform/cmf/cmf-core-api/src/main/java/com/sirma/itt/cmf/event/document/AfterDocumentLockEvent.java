package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document has been locked, before document save.
 * 
 * @author BBonev
 */
@Documentation("Event fired after document has been locked, before document save.")
public class AfterDocumentLockEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/** The locked by. */
	private final String lockedBy;

	/**
	 * Instantiates a new after document lock event.
	 * 
	 * @param instance
	 *            the instance
	 * @param lockedBy
	 *            the locked by
	 */
	public AfterDocumentLockEvent(DocumentInstance instance, String lockedBy) {
		super(instance);
		this.lockedBy = lockedBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
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
