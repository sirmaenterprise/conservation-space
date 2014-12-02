package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after document has been copied to his new destination. The event object provides old
 * and new document instances.<br>
 * <b>NOTE:</b> The same type of event will be fired for all different types of document copying:
 * <ul>
 * <li>copy content
 * <li>copy as link: to fixed version or to current version
 * </ul>
 * 
 * @author BBonev
 */
@Documentation("Event fired after document has been copied to his new destination. "
		+ "The event object provides old and new document instances."
		+ "<br><b>NOTE:</b> The same type of event will be fired for all different types of document copying:"
		+ "<ul><li>copy content<li>copy as link: to fixed version or to current version</ul>")
public class AfterDocumentCopyEvent extends
		AbstractInstanceTwoPhaseEvent<DocumentInstance, TwoPhaseEvent> {

	/** The is as link. */
	private final boolean isAsLink;

	/** The is hard link. */
	private final boolean isHardLink;

	/** The new instance. */
	private DocumentInstance newInstance;

	/**
	 * Instantiates a new after document copy event.
	 * 
	 * @param oldInstance
	 *            the instance
	 * @param newInstance
	 *            the new instance
	 * @param isAsLink
	 *            the is as link
	 * @param isHardLink
	 *            the is hard link
	 */
	public AfterDocumentCopyEvent(DocumentInstance oldInstance, DocumentInstance newInstance,
			boolean isAsLink, boolean isHardLink) {
		super(oldInstance);
		setNewInstance(newInstance);
		this.isAsLink = isAsLink;
		this.isHardLink = isHardLink;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
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

	/**
	 * Getter method for newInstance.
	 * 
	 * @return the newInstance
	 */
	public DocumentInstance getNewInstance() {
		return newInstance;
	}

	/**
	 * Setter method for newInstance.
	 * 
	 * @param newInstance
	 *            the newInstance to set
	 */
	public void setNewInstance(DocumentInstance newInstance) {
		this.newInstance = newInstance;
	}

}
