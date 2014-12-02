package com.sirma.itt.cmf.event.document.structured;

import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when the structured document is opened by the user.
 * 
 * @author BBonev
 */
@Documentation("Event fired when the structured document is opened by the user.")
public class StructuredDocumentOpenEvent extends StructuredDocumentOperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -526594130324721059L;

	/**
	 * Instantiates a new structured document open event.
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
	public StructuredDocumentOpenEvent(DocumentInstance instance, String documentType,
			LocalFileDescriptor structuredDocumentContent, boolean previewMode) {
		super(instance, documentType, structuredDocumentContent, previewMode);
	}

}
