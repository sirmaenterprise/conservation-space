package com.sirma.itt.cmf.event.document.structured;

import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when the given structured document need to be printed.
 * 
 * @author BBonev
 */
@Documentation("Event fired when the given structured document need to be printed.")
public class StructuredDocumentPrintEvent extends StructuredDocumentOperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8649169369816669178L;

	/**
	 * Instantiates a new structured document print event.
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
	public StructuredDocumentPrintEvent(DocumentInstance instance, String documentType,
			LocalFileDescriptor structuredDocumentContent, boolean previewMode) {
		super(instance, documentType, structuredDocumentContent, previewMode);
	}

}
