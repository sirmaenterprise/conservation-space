package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that the given document has been reverted. The event is fired after DMS
 * revert before document update.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that the given document has been reverted. The event is fired after DMS revert before document update.")
public class AfterDocumentRevertEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

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
	public AfterDocumentRevertEvent(DocumentInstance instance, String revertedToVersion) {
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
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}
}
