package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that the given document is going to be reverted. The event is fired before
 * DMS revert operation.
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired to notify that the given document is going to be reverted. The event is fired before DMS revert operation.")
public class BeforeDocumentRevertEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentRevertEvent> {

	/** The reverted to version. */
	private final String revertedToVersion;

	/**
	 * Instantiates a new document revert event.
	 *
	 * @param instance
	 *            the instance
	 * @param revertedToVersion
	 *            the reverted to version
	 */
	public BeforeDocumentRevertEvent(DocumentInstance instance, String revertedToVersion) {
		super(instance);
		this.revertedToVersion = revertedToVersion;
	}

	/**
	 * Getter method for revertedToVersion.
	 *
	 * @return the revertedToVersion
	 */
	public String getRevertedToVersion() {
		return revertedToVersion;
	}

	@Override
	protected AfterDocumentRevertEvent createNextEvent() {
		return new AfterDocumentRevertEvent(getInstance(), revertedToVersion);
	}
}
