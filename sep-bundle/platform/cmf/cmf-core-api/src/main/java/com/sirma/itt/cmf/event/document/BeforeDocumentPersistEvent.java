package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before the document instance is persisted for the first time
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before the document instance is persisted for the first time")
public class BeforeDocumentPersistEvent extends
		BeforeInstancePersistEvent<DocumentInstance, AfterDocumentPersistEvent> {

	/**
	 * Instantiates a new after document persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeDocumentPersistEvent(DocumentInstance instance) {
		super(instance);
	}

	@Override
	protected AfterDocumentPersistEvent createNextEvent() {
		return new AfterDocumentPersistEvent(getInstance());
	}

}
