package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the document.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the document.")
public class AttachedChildToDocumentEvent extends InstanceAttachedEvent<DocumentInstance> {

	/**
	 * Instantiates a new attached child to document event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToDocumentEvent(DocumentInstance target, Instance child) {
		super(target, child);
	}

}
