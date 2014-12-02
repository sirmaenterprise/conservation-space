package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new document instance is created
 * 
 * @author BBonev
 */
@Documentation("Event fired when new document instance is created")
public class DocumentCreateEvent extends InstanceCreateEvent<DocumentInstance> {

	/**
	 * Instantiates a new document create event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public DocumentCreateEvent(DocumentInstance instance) {
		super(instance);
	}

}
