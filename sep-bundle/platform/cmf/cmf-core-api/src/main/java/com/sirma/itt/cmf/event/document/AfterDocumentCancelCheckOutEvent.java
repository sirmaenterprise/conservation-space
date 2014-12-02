package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document cancel check out from DMS.
 * 
 * @author BBonev
 */
@Documentation("Event fired after document cancel check out from DMS.")
public class AfterDocumentCancelCheckOutEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document check out event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentCancelCheckOutEvent(DocumentInstance instance) {
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
