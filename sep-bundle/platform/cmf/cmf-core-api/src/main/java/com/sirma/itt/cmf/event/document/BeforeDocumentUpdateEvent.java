package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;

/**
 * Event fired before document properties update
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public class BeforeDocumentUpdateEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentUpdateEvent> {

	/**
	 * Instantiates a new before document update event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentUpdateEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentUpdateEvent createNextEvent() {
		return new AfterDocumentUpdateEvent(getInstance());
	}

}
