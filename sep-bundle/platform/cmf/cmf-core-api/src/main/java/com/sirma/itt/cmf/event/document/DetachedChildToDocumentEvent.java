package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the document.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the document.")
public class DetachedChildToDocumentEvent extends InstanceDetachedEvent<DocumentInstance> {

	/**
	 * Instantiates a new detached child to document event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToDocumentEvent(DocumentInstance target, Instance child) {
		super(target, child);
	}

}
