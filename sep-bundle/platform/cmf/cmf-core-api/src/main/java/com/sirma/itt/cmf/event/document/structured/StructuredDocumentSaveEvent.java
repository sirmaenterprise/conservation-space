package com.sirma.itt.cmf.event.document.structured;

import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to initialize save of the given structured document.
 * 
 * @author BBonev
 */
@Documentation("Event fired to initialize save of the given structured document.")
public class StructuredDocumentSaveEvent extends StructuredDocumentOperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8814613378682577284L;

	/**
	 * Instantiates a new structured document save event.
	 * 
	 * @param instance
	 *            the instance
	 * @param documentType
	 *            the document type
	 * @param structuredDocumentContent
	 *            the structured document content
	 * @param previewMode
	 *            the preview mode
	 */
	public StructuredDocumentSaveEvent(DocumentInstance instance, String documentType,
			LocalFileDescriptor structuredDocumentContent, boolean previewMode) {
		super(instance, documentType, structuredDocumentContent, previewMode);
	}

}
