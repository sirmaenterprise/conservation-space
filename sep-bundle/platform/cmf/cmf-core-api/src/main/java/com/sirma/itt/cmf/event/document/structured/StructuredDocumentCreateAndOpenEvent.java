package com.sirma.itt.cmf.event.document.structured;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when the given structured document need to be created and visualized in edit mode.
 * 
 * @author BBonev
 */
@Documentation("Event fired when the given structured document need to be created and visualized in edit mode.")
public class StructuredDocumentCreateAndOpenEvent extends StructuredDocumentOperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -425391506284391910L;

	/**
	 * Instantiates a new structured document create and open event.
	 * 
	 * @param instance
	 *            the instance
	 * @param documentType
	 *            the document type
	 */
	public StructuredDocumentCreateAndOpenEvent(DocumentInstance instance, String documentType) {
		super(instance, documentType, null, false);
	}
}
