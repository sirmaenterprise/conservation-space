package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;

/**
 * Handles creating of new documents for a specific section through the user interface.
 *
 * @author Adrian Mitev
 */
@Named("createDocumentAction")
@ViewAccessScoped
public class CreateHtmlDocumentAction extends NewDocumentAction implements Serializable {

	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

	/**
	 * File title manually input by the user.
	 */
	private String manualTitle;

	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE, defaultValue = "OT210027")
	private String documentDefaultType;

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -8163240825770840231L;

	/**
	 * Prepares the creation of new file.
	 *
	 * @param selectedSection
	 *            section where the new file will be added.
	 */
	public void prepare(SectionInstance selectedSection) {
		setSectionInstance(selectedSection);

		filterFileTypesByCase(selectedSection, null);
	}

	/**
	 * Creates a new empty file, sets its properties and opens it for edit.
	 *
	 * @return navigation case to document details page where the document will be edited.
	 */
	public String create() {
		DocumentInstance newDocumentInstance = createDocumentInstance();

		return caseDocumentsTableAction.openForEdit(newDocumentInstance);
	}

	/**
	 * Creates a new DocumentInstance and make it an HTML document.
	 *
	 * @return created documentInstance.
	 */
	protected DocumentInstance createDocumentInstance() {
		// the title field is used for document type
		DocumentInstance newDocumentInstance = documentService.createDocumentInstance(
				getSectionInstance(), getTitle());

		Map<String, Serializable> properties = newDocumentInstance.getProperties();
		String name = getFileName();
		// ensure file extension
		if (!name.toLowerCase().endsWith(".xml")) {
			name += ".xml";
		}
		// set empty content
		properties.put(DocumentProperties.NAME, name);
		properties.put(DocumentProperties.DESCRIPTION, getDescription());
		// if the document is free-style document (no explicit type), use the
		// manual title
		if (getTitle().equals(documentDefaultType) && StringUtils.isNotNullOrEmpty(manualTitle)) {
			properties.put(DocumentProperties.TITLE, manualTitle);
		} else {
			properties.put(DocumentProperties.TITLE, getTitle());
		}

		// populate the document with empty content;
		properties.put(DocumentProperties.MIMETYPE, "text/html");

		// set the new instance as current
		getDocumentContext().setCurrentInstance(newDocumentInstance);
		return newDocumentInstance;
	}

	/**
	 * Getter method for manualTitle.
	 *
	 * @return the manualTitle
	 */
	public String getManualTitle() {
		return manualTitle;
	}

	/**
	 * Setter method for manualTitle.
	 *
	 * @param manualTitle
	 *            the manualTitle to set
	 */
	public void setManualTitle(String manualTitle) {
		this.manualTitle = manualTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterUploadAction() {
		// TODO Auto-generated method stub
	}

}
