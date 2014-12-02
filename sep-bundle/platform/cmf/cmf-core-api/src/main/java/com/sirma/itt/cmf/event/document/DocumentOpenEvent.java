package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before document to be visualized on screen
 * 
 * @author BBonev
 */
@Documentation("Event fired before document to be visualized on screen")
public class DocumentOpenEvent extends InstanceOpenEvent<DocumentInstance> {

	/**
	 * Instantiates a new document open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public DocumentOpenEvent(DocumentInstance instance) {
		super(instance);
	}

}
