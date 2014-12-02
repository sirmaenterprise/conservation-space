package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document has been unlocked and before document save.
 *
 * @author BBonev
 */
@Documentation("Event fired after document has been unlocked and before document save.")
public class AfterDocumentUnlockEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after document unlock event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterDocumentUnlockEvent(DocumentInstance instance) {
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
