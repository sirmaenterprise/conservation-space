package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;

/**
 * Event fired after document properties update and after save. The event should not update document
 * instance due to instance will not be saved!
 * 
 * @author BBonev
 */
public class AfterDocumentUpdateEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document update event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentUpdateEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

}
