package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document to be deleted from the system. Any modifications to the document
 * instance will not be recorded!
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document to be deleted from the system. Any modifications to the document instance will not be recorded!")
public class BeforeDocumentDeleteEvent extends
		BeforeInstanceDeleteEvent<DocumentInstance, AfterDocumentDeleteEvent> {

	/**
	 * Instantiates a new before document delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentDeleteEvent(DocumentInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentDeleteEvent createNextEvent() {
		return new AfterDocumentDeleteEvent(getInstance());
	}

}
