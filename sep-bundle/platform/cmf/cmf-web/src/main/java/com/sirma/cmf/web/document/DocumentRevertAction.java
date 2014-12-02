package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;

/**
 * Responsible for revert document to previous versions.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentRevertAction extends Action implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1561591188194522161L;

	/**
	 * Historical document version.
	 */
	private String documentVersion;

	/**
	 * Historical document instance.
	 */
	private DocumentInstance historicalDocument;

	/**
	 * Reverted document description.
	 */
	private String description;

	/**
	 * Reverted document version.
	 */
	private boolean majorVersion;

	/**
	 * DocumentServiceImpl instance.
	 */
	@Inject
	private DocumentService documentService;

	/**
	 * CaseDocumentsTableAction instance.
	 */
	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

	/**
	 * Initializes bean properties upon version revert pop-up display.
	 * 
	 * @param historicalDocument
	 *            historical document version
	 * @param documentVersion
	 *            document version
	 */
	public void initVersionRevert(DocumentInstance historicalDocument, String documentVersion) {
		log.debug("CMFWeb: Executing DocumentRevertAction.initVersionRevert - document version ["
				+ documentVersion + "] old document instance id[" + historicalDocument.getId()
				+ "]");

		this.majorVersion = false;
		this.description = "";
		this.documentVersion = documentVersion;
		// save reference to latest version
		if (getDocumentContext().get(DocumentConstants.DOCUMENT_LATEST_VERSION) == null) {
			getDocumentContext().put(DocumentConstants.DOCUMENT_LATEST_VERSION, historicalDocument);
		}
		this.historicalDocument = historicalDocument;
	}

	/**
	 * Executes document revert to previous version.
	 * 
	 * @return navigation string
	 */
	public String executeDocumentRevert() {
		log.debug("CMFWeb: Executing DocumentRevertAction.executeDocumentRevert");
		DocumentInstance currentDocInstance = (DocumentInstance) getDocumentContext().get(
				DocumentConstants.DOCUMENT_LATEST_VERSION);

		Map<String, Serializable> currentDocumentProperties = currentDocInstance.getProperties();
		currentDocumentProperties.put(DocumentProperties.IS_MAJOR_VERSION, this.majorVersion);
		currentDocumentProperties.put(DocumentProperties.VERSION_DESCRIPTION, this.description);

		DocumentInstance revertedInstance = documentService.revertVersion(currentDocInstance,
				this.documentVersion);

		return caseDocumentsTableAction.open(revertedInstance);
	}

	/**
	 * Getter method for documentVersion.
	 * 
	 * @return the documentVersion
	 */
	public String getDocumentVersion() {
		return documentVersion;
	}

	/**
	 * Setter method for documentVersion.
	 * 
	 * @param documentVersion
	 *            the documentVersion to set
	 */
	public void setDocumentVersion(String documentVersion) {
		this.documentVersion = documentVersion;
	}

	/**
	 * Getter method for historicalDocument.
	 * 
	 * @return the historicalDocument
	 */
	public DocumentInstance getHistoricalDocument() {
		return historicalDocument;
	}

	/**
	 * Setter method for historicalDocument.
	 * 
	 * @param historicalDocument
	 *            the historicalDocument to set
	 */
	public void setHistoricalDocument(DocumentInstance historicalDocument) {
		this.historicalDocument = historicalDocument;
	}

	/**
	 * Getter method for description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter method for description.
	 * 
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Getter method for majorVersion.
	 * 
	 * @return the majorVersion
	 */
	public boolean isMajorVersion() {
		return majorVersion;
	}

	/**
	 * Setter method for majorVersion.
	 * 
	 * @param majorVersion
	 *            the majorVersion to set
	 */
	public void setMajorVersion(boolean majorVersion) {
		this.majorVersion = majorVersion;
	}

}
