package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after the document instance is persisted for the first time
 * 
 * @author BBonev
 */
@Documentation("Event fired after the document instance is persisted for the first time")
public class AfterDocumentPersistEvent extends
		AfterInstancePersistEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentPersistEvent(DocumentInstance instance) {
		super(instance);
	}

}
