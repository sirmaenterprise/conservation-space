package com.sirma.cmf.web.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.SelectorItem;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Pair;

/**
 * Contains common logic used when adding new document through the user interface.
 * 
 * @author Adrian Mitev
 */
public abstract class NewDocumentAction extends EntityAction {

	/** The document title cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_TITLE)
	private Integer documentTitleCL;

	/** The document default type. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_DOCUMENT_DEFAULT_ATTACHMENT_TYPE)
	private String documentDefaultType;

	/**
	 * Name of the new file.
	 */
	protected String fileName;

	/**
	 * Document description field.
	 */
	private String description;

	/**
	 * Document title field.
	 */
	private String title;

	/**
	 * Section instance that where the file new will be created.
	 */
	private SectionInstance sectionInstance;

	/**
	 * Available file types associated with/allowed for the selected section.
	 */
	protected List<SelectorItem> fileTypes;

	/**
	 * After upload action that will be called from upload listener immediately after the file is
	 * uploaded.
	 */
	public abstract void afterUploadAction();

	/**
	 * Filter file types by case type.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @param purpose
	 *            filter only documents with a specific purpose
	 */
	protected void filterFileTypesByCase(SectionInstance sectionInstance, String purpose) {
		List<SelectorItem> fileTypeList = new ArrayList<SelectorItem>();

		Map<String, CodeValue> codeValues = codelistService.getCodeValues(documentTitleCL);

		// get allowed documents for selected section
		Map<String, Pair<DocumentDefinitionRef, Integer>> allowedDocuments = documentService
				.getAllowedDocuments(sectionInstance);

		// filter codevalues that are not set in document definition for the
		// section
		Map<String, CodeValue> filteredCodevalues = new HashMap<String, CodeValue>();
		for (Entry<String, Pair<DocumentDefinitionRef, Integer>> entry : allowedDocuments
				.entrySet()) {
			String key = entry.getKey();
			String definitionPurpose = entry.getValue().getFirst().getPurpose();
			if (codeValues.containsKey(key)) {
				// only show documents definitions that have the specified purpose or don't have any
				// purpose at all when no purpose is specified (purpose param == null)
				if (((definitionPurpose == null) && (purpose == null))
						|| ((definitionPurpose != null) && (purpose != null) && definitionPurpose
								.contains(purpose))) {
					filteredCodevalues.put(key, codeValues.get(key));
				}
			}
		}

		for (String key : filteredCodevalues.keySet()) {
			CodeValue codeValue = codeValues.get(key);

			String description = codelistService.getDescription(documentTitleCL,
					codeValue.getValue());

			String numberOfCopies = Integer.toString(allowedDocuments.get(key).getSecond());

			// unlimited number of documents can be uploaded
			if ("-1".equals(numberOfCopies)) {
				numberOfCopies = labelProvider
						.getValue(LabelConstants.DOCUMENT_UPLOAD_NUMBER_OF_COPIES_UNLIMITED);
			}

			fileTypeList
					.add(new SelectorItem(key, key + "  (" + numberOfCopies + ")", description));
		}

		fileTypes = fileTypeList;
	}

	/**
	 * Exposes the DEFAULT_DOCUMENT_ATTACHMENT_TYPE constant.
	 * 
	 * @return value of the DEFAULT_DOCUMENT_ATTACHMENT_TYPE constant.
	 */
	public String getDefaultDocumentType() {
		return documentDefaultType;
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
	 * Getter method for title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter method for title.
	 * 
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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
	 * @param sectionInstance
	 *            the sectionInstance to set
	 */
	public void setSectionInstance(SectionInstance sectionInstance) {
		this.sectionInstance = sectionInstance;
	}

	/**
	 * Getter method for fileTypes.
	 * 
	 * @return the fileTypes
	 */
	public List<SelectorItem> getFileTypes() {
		return fileTypes;
	}

	/**
	 * Setter method for fileTypes.
	 * 
	 * @param fileTypes
	 *            the fileTypes to set
	 */
	public void setFileTypes(List<SelectorItem> fileTypes) {
		this.fileTypes = fileTypes;
	}

	/**
	 * Getter method for fileName.
	 * 
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Setter method for fileName.
	 * 
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
