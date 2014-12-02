package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document to be moved to new parent. <br>
 * <b>NOTE</b>: after constructing the next event phase the caller need to set the correct new
 * {@link DocumentInstance} before to fire the event.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before document to be moved to new parent. "
		+ "<br> <b>NOTE</b>: after constructing the next event phase the "
		+ "caller need to set the correct new {@link DocumentInstance} before to fire the event.")
public class BeforeDocumentMoveEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentMoveEvent> {

	/** The source instance. */
	private final Instance sourceInstance;
	/** The target instance. */
	private final SectionInstance targetInstance;

	/**
	 * Instantiates a new before document move event.
	 * 
	 * @param oldInstance
	 *            the old instance
	 * @param sourceInstance
	 *            the source instance from which the document is moved
	 * @param targetInstance
	 *            the target instance where document is moved
	 */
	public BeforeDocumentMoveEvent(DocumentInstance oldInstance, Instance sourceInstance,
			SectionInstance targetInstance) {
		super(oldInstance);
		this.sourceInstance = sourceInstance;
		this.targetInstance = targetInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentMoveEvent createNextEvent() {
		return new AfterDocumentMoveEvent(getInstance(), getSourceInstance(), getTargetInstance());
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
