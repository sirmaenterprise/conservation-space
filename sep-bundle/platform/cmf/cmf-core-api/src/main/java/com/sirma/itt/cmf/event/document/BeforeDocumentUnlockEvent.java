package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document to be unlocked.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document to be unlocked.")
public class BeforeDocumentUnlockEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentUnlockEvent> {

	/**
	 * Instantiates a new before document unlock event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentUnlockEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentUnlockEvent createNextEvent() {
		return new AfterDocumentUnlockEvent(getInstance());
	}

}
