package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on document change to trigger document/case save.
 * 
 * @author BBonev
 */
@Documentation("Event fired on document change to trigger document/case save.")
public class DocumentChangeEvent extends InstanceChangeEvent<DocumentInstance> {

	/**
	 * Instantiates a new document change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public DocumentChangeEvent(DocumentInstance instance) {
		super(instance);
	}

}
