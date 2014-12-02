package com.sirma.cmf.web.workflow.task;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;

/**
 * The Class TaskDocument represents a document instance that is attached to a task as incoming
 * document or is uploaded trough the task.
 * 
 * @author svelikov
 */
public class TaskDocument {

	/**
	 * If the outgoing document is already uploaded to the case, that means the document can be
	 * accessed directly trough a link from the task.
	 */
	private boolean isUploaded;

	/**
	 * If the incoming document is already attached to the task, that means the document can be
	 * accessed trough a link from the task.
	 */
	private boolean isAttached;

	/** The document instance. */
	private DocumentInstance documentInstance;

	/** The section instance. */
	private SectionInstance sectionInstance;

	/** The document type. */
	private String documentType;

	/** The document type description. */
	private String documentTypeDescription;

	/**
	 * Getter method for isUploaded.
	 * 
	 * @return the isUploaded
	 */
	public boolean isUploaded() {
		return isUploaded;
	}

	/**
	 * Setter method for isUploaded.
	 * 
	 * @param uploaded
	 *            the isUploaded to set
	 */
	public void setUploaded(boolean uploaded) {
		this.isUploaded = uploaded;
	}

	/**
	 * Getter method for isAttached.
	 * 
	 * @return the isAttached
	 */
	public boolean isAttached() {
		return isAttached;
	}

	/**
	 * Setter method for isAttached.
	 * 
	 * @param attached
	 *            the isAttached to set
	 */
	public void setAttached(boolean attached) {
		this.isAttached = attached;
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
	 * @param instance
	 *            the documentInstance to set
	 */
	public void setDocumentInstance(DocumentInstance instance) {
		this.documentInstance = instance;
	}

	/**
	 * Getter method for sectionInstance.
	 * 
	 * @return the sectionInstance
	 */
	public SectionInstance getSectionInstance() {
		return sectionInstance;
	}

	/**
	 * Setter method for sectionInstance.
	 * 
	 * @param instance
	 *            the sectionInstance to set
	 */
	public void setSectionInstance(SectionInstance instance) {
		this.sectionInstance = instance;
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
	 * @param type
	 *            the documentType to set
	 */
	public void setDocumentType(String type) {
		this.documentType = type;
	}

	/**
	 * Getter method for documentTypeDescription.
	 * 
	 * @return the documentTypeDescription
	 */
	public String getDocumentTypeDescription() {
		return documentTypeDescription;
	}

	/**
	 * Setter method for documentTypeDescription.
	 * 
	 * @param description
	 *            the documentTypeDescription to set
	 */
	public void setDocumentTypeDescription(String description) {
		this.documentTypeDescription = description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "TaskDocument [isUploaded=" + isUploaded + ", isAttached=" + isAttached
				+ ", documentInstance=" + documentInstance + ", sectionInstance=" + sectionInstance
				+ ", documentType=" + documentType + ", documentTypeDescription="
				+ documentTypeDescription + "]";
	}

}
