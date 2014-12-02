package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document has been moved to the new destination. The instance has been attached
 * to his new parent before the event has been fired.
 * 
 * @author BBonev
 */
@Documentation("Event fired after document has been moved to the new destination. "
		+ "The instance has been attached to his new parent before the event has been fired.")
public class AfterDocumentMoveEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/** The source instance. */
	private final Instance sourceInstance;
	/** The target instance. */
	private final SectionInstance targetInstance;

	/**
	 * Instantiates a new after document move event.
	 * 
	 * @param instance
	 *            is moved instance
	 * @param sourceInstance
	 *            the source instance
	 * @param targetInstance
	 *            the target instance
	 */
	public AfterDocumentMoveEvent(DocumentInstance instance, Instance sourceInstance,
			SectionInstance targetInstance) {
		super(instance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	/**
	 * Getter method for sourceInstance.
	 * 
	 * @return the sourceInstance
	 */
	public Instance getSourceInstance() {
		return sourceInstance;
	}

	/**
	 * Getter method for targetInstance.
	 * 
	 * @return the targetInstance
	 */
	public SectionInstance getTargetInstance() {
		return targetInstance;
	}

}
