package com.sirma.itt.objects.web.caseinstance.section;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.definition.DefinitionUtil;

/**
 * Backing bean for object sections.
 * 
 * @author svelikov
 */
@Named
public class SectionObjectsAction extends Action implements Serializable {

	private static final long serialVersionUID = -12730373484897225L;

	private static final String SECTION_PURPOSE = "objectsSection";

	private List<SectionInstance> caseObjectSections;

	/**
	 * Finds out the case section for given tab by filtering them by purpose.
	 */
	@PostConstruct
	public void init() {
		CaseInstance instance = getDocumentContext().getInstance(CaseInstance.class);
		if (instance != null) {
			List<SectionInstance> sections = instance.getSections();
			caseObjectSections = DefinitionUtil.filterByPurpose(sections, SECTION_PURPOSE);
		} else {
			log.error("ObjectsWeb: A case instance was not found in context!");
		}
	}

	/**
	 * Getter method for caseObjectSections.
	 * 
	 * @return the caseObjectSections
	 */
	public List<SectionInstance> getCaseObjectSections() {
		return caseObjectSections;
	}

	/**
	 * Setter method for caseObjectSections.
	 * 
	 * @param caseObjectSections
	 *            the caseObjectSections to set
	 */
	public void setCaseObjectSections(List<SectionInstance> caseObjectSections) {
		this.caseObjectSections = caseObjectSections;
	}

}
