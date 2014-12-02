package com.sirma.itt.cmf.event.document.structured;

import java.io.Serializable;

import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event object for working with structured documents.<br>
 * The event must be fired with filled at least request document ID. If the request is handled then
 * at least the flag handled should be changed to <code>true</code>. Depending on the operation as
 * return data can be provided the {@link #targetUri} or {@link #documentResponse}
 * 
 * @author BBonev
 */
@Documentation("Event object for working with structured documents.<br> The event must be fired with filled at least request document ID. If the request is handled then at least the flag handled should be changed to <code>true</code>. Depending on the operation as return data can be provided the targetUri or documentResponse")
public abstract class StructuredDocumentOperationEvent extends
		AbstractInstanceEvent<DocumentInstance> implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6788390782150044741L;

	/** The document type. */
	private String documentType;

	/** Whether document should be rendered in preview mode or edit. */
	private boolean previewMode;

	/** The navigation string. */
	private String navigationPath;

	/** The handled. */
	private boolean handled;

	/** The document request. */
	private FileDescriptor documentRequest;

	/** The document response. */
	private FileDescriptor documentResponse;

	/** The {@link DocumentInstance}. */
	private DocumentInstance documentInstance;

	/**
	 * Instantiates a new structured document operation event.
	 * 
	 * @param instance
	 *            the instance
	 * @param documentType
	 *            the document type
	 */
	public StructuredDocumentOperationEvent(DocumentInstance instance, String documentType) {
		super(instance);
		this.documentType = documentType;
	}

	/**
	 * Instantiates a new structured document operation event.
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
	public StructuredDocumentOperationEvent(DocumentInstance instance, String documentType,
			LocalFileDescriptor structuredDocumentContent, boolean previewMode) {
		super(instance);
		this.documentType = documentType;
		documentRequest = structuredDocumentContent;
		this.previewMode = previewMode;
	}

	/**
	 * Getter method for handled.
	 * 
	 * @return the handled
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * Setter method for handled.
	 * 
	 * @param handled
	 *            the handled to set
	 */
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	/**
	 * Getter method for document.
	 * 
	 * @return the document
	 */
	public FileDescriptor getDocumentRequest() {
		return documentRequest;
	}

	/**
	 * Setter method for document.
	 * 
	 * @param document
	 *            the document to set
	 */
	public void setDocumentRequest(FileDescriptor document) {
		documentRequest = document;
	}

	/**
	 * Getter method for documentResponse.
	 * 
	 * @return the documentResponse
	 */
	public FileDescriptor getDocumentResponse() {
		return documentResponse;
	}

	/**
	 * Setter method for documentResponse.
	 * 
	 * @param documentResponse
	 *            the documentResponse to set
	 */
	public void setDocumentResponse(FileDescriptor documentResponse) {
		this.documentResponse = documentResponse;
	}

	/**
	 * Getter method for documentInstance.
	 * 
	 * @return the documentInstance
	 */
	public DocumentInstance getDocumentInstance() {
		return documentInstance;
	}

	/**
	 * Setter method for documentInstance.
	 * 
	 * @param documentInstance
	 *            the documentInstance to set
	 */
	public void setDocumentInstance(DocumentInstance documentInstance) {
		this.documentInstance = documentInstance;
	}

	/**
	 * Getter method for documentType.
	 * 
	 * @return the documentType
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * Setter method for documentType.
	 * 
	 * @param documentType
	 *            the documentType to set
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * Getter method for navigationPath.
	 * 
	 * @return the navigationPath
	 */
	public String getNavigationPath() {
		return navigationPath;
	}

	/**
	 * Setter method for navigationPath.
	 * 
	 * @param navigationPath
	 *            the navigationPath to set
	 */
	public void setNavigationPath(String navigationPath) {
		this.navigationPath = navigationPath;
	}

	/**
	 * Getter method for previewMode.
	 * 
	 * @return the previewMode
	 */
	public boolean isPreviewMode() {
		return previewMode;
	}

	/**
	 * Setter method for previewMode.
	 * 
	 * @param previewMode
	 *            the previewMode to set
	 */
	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}

}
