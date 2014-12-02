package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after a successful document deletion.
 * 
 * @author BBonev
 */
@Documentation("Event fired after a successful document deletion.")
public class AfterDocumentDeleteEvent extends
		AfterInstanceDeleteEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentDeleteEvent(DocumentInstance instance) {
		super(instance);
	}
}
