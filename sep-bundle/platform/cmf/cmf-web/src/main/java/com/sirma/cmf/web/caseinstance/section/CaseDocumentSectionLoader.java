package com.sirma.cmf.web.caseinstance.section;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.tab.CaseSectionsActionBase;
import com.sirma.itt.cmf.beans.model.SectionInstance;

/**
 * Sections loader for document tab. On initialization will retrieve and prepare the document
 * section for specific case and their content.
 * 
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class CaseDocumentSectionLoader extends CaseSectionsActionBase implements Serializable {

	private static final long serialVersionUID = -4583215888030521796L;

	/** The placeholder, represent document section definition identifier. */
	private static final String CASE_DOCUMENT_SECTION_IDENTIFIER = "casedashboard_document_section_tab";

	/** The counter for case document sections. */
	private static final String CASE_DOCUMENT_SECTION_COUNTER = "casedashboard.document.section.counter";

	/**
	 * Service that will be used for retrieving document sections instances.
	 */
	@Inject
	private SectionDocumentsAction section;

	/** Holds all document sections. */
	private List<SectionInstance> sectionList;

	/**
	 * Initializing document sections and addition content data.
	 */
	@PostConstruct
	public void initData() {
		onOpen();
		setSectionList(section.getCaseDocumentSections());
	}

	@Override
	public String getSectionIdentifier() {
		return CASE_DOCUMENT_SECTION_IDENTIFIER;
	}

	@Override
	public String getCounterBundle() {
		return CASE_DOCUMENT_SECTION_COUNTER;
	}

	/**
	 * Setter for document sections.
	 * 
	 * @return list with document section
	 */
	public List<SectionInstance> getSectionList() {
		return sectionList;
	}

	/**
	 * Setter for document sections.
	 * 
	 * @param sectionList
	 *            available document sections.
	 */
	public void setSectionList(List<SectionInstance> sectionList) {
		this.sectionList = sectionList;
	}

}
