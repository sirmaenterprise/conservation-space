package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event object fired before document to be copied.<br>
 * <b>NOTE</b>: after constructing the next event phase the caller need to set the correct new
 * {@link DocumentInstance} before to fire the event.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event object fired before document to be copied."
		+ "<br><b>NOTE</b>: after constructing the next event phase the caller need to set the correct new "
		+ "{@link DocumentInstance} before to fire the event.")
public class BeforeDocumentCopyEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, AfterDocumentCopyEvent> {

	/** The is as link. */
	private final boolean isAsLink;

	/** The is hard link. */
	private final boolean isHardLink;

	/**
	 * Instantiates a new before document copy event.
	 * 
	 * @param instance
	 *            the instance
	 * @param isAsLink
	 *            the is as link
	 * @param isHardLink
	 *            the is hard link
	 */
	public BeforeDocumentCopyEvent(DocumentInstance instance, boolean isAsLink, boolean isHardLink) {
		super(instance);
		this.isAsLink = isAsLink;
		this.isHardLink = isHardLink;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterDocumentCopyEvent createNextEvent() {
		return new AfterDocumentCopyEvent(getInstance(), null, isAsLink(), isHardLink());
	}

	/**
	 * Getter method for isAsLink.
	 * 
	 * @return the isAsLink
	 */
	public boolean isAsLink() {
		return isAsLink;
	}

	/**
	 * Getter method for isHardLink.
	 * 
	 * @return the isHardLink
	 */
	public boolean isHardLink() {
		return isHardLink;
	}

}
