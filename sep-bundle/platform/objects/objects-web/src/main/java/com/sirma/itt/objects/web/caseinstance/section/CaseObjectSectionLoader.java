package com.sirma.itt.objects.web.caseinstance.section;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.tab.CaseSectionsActionBase;
import com.sirma.itt.cmf.beans.model.SectionInstance;

/**
 * Sections loader for object tab. On initialization will retrieve and prepare the object section
 * for specific case and their content.
 * 
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class CaseObjectSectionLoader extends CaseSectionsActionBase implements Serializable {

	private static final long serialVersionUID = -4583215888030521796L;

	/** The placeholder for object sections, represent object definition identifier. */
	private static final String CASE_OBJECT_SECTION_IDENTIFIER = "casedashboard_object_section_tab";

	/** The object section counter bundle. */
	private static final String CASE_OBJECT_SECTION_COUNTER = "casedashboard.object.section.counter";

	/** Service for retrieving all available object sections. */
	@Inject
	private SectionObjectsAction sectionObjectAction;

	/** List with found object sections. */
	private List<SectionInstance> sectionList;

	/**
	 * Initializing object sections and addition content data.
	 */
	@PostConstruct
	public void initData() {
		onOpen();
		setSectionList(sectionObjectAction.getCaseObjectSections());
	}

	@Override
	public String getSectionIdentifier() {
		return CASE_OBJECT_SECTION_IDENTIFIER;
	}

	@Override
	public String getCounterBundle() {
		return CASE_OBJECT_SECTION_COUNTER;
	}

	/**
	 * Getter for object sections.
	 * 
	 * @return list with available object sections
	 */
	public List<SectionInstance> getSectionList() {
		return sectionList;
	}

	/**
	 * Setter for supported object sections.
	 * 
	 * @param sectionList
	 *            list with object sections
	 */
	public void setSectionList(List<SectionInstance> sectionList) {
		this.sectionList = sectionList;
	}

}
