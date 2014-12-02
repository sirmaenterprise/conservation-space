package com.sirma.itt.cmf.event.document.structured;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when the structured document need to be created but is not going to be visualized.
 * 
 * @author BBonev
 */
@Documentation("Event fired when the structured document need to be created but is not going to be visualized")
public class StructuredDocumentCreateNewEmptyEvent extends StructuredDocumentOperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6977366594010732743L;

	/**
	 * Instantiates a new structured document create new empty event.
	 * 
	 * @param instance
	 *            the instance
	 * @param documentType
	 *            the document type
	 */
	public StructuredDocumentCreateNewEmptyEvent(DocumentInstance instance, String documentType) {
		super(instance, documentType);
	}
}
