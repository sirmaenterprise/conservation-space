package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document cancel check out from DMS.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document cancel check out from DMS.")
public class BeforeDocumentCancelCheckOutEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentCancelCheckOutEvent> {

	/**
	 * Instantiates a new before document check out event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentCancelCheckOutEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentCancelCheckOutEvent createNextEvent() {
		return new AfterDocumentCancelCheckOutEvent(getInstance());
	}
}
