package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;

/**
 * Event fired before document instance to be uploaded to DMS system.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public class BeforeDocumentUploadEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentUploadEvent> {

	/**
	 * Instantiates a new before document upload event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentUploadEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentUploadEvent createNextEvent() {
		return new AfterDocumentUploadEvent(getInstance());
	}
}
