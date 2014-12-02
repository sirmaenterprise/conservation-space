package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;

/**
 * Event fired after successful document upload before document propeties save.
 * 
 * @author BBonev
 */
public class AfterDocumentUploadEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document upload event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentUploadEvent(DocumentInstance instance) {
		super(instance);
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}
}
