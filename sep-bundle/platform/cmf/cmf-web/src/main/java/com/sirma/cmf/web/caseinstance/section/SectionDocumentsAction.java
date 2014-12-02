package com.sirma.cmf.web.caseinstance.section;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Named;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * Backing bean for document sections.
 * 
 * @author svelikov
 */
@Named
public class SectionDocumentsAction extends EntityAction implements Serializable {

	private static final long serialVersionUID = 9170441144541731128L;

	private List<SectionInstance> caseDocumentSections;

	/**
	 * Finds out the case section for given tab by filtering them by purpose.
	 */
	@PostConstruct
	public void init() {
		loadSectionDocuments();
	}

	/**
	 * Load section documents.
	 */
	private void loadSectionDocuments() {
		Instance currentInstance = getDocumentContext().getCurrentInstance();
		if (currentInstance == null) {
			log.error("Can not load context instance for missing current instance in context.");
			return;
		}
		CaseInstance instance = (CaseInstance) InstanceUtil.getContext(currentInstance);
		if (instance == null) {
			caseDocumentSections = new LinkedList<>();
			return;
		}
		refreshInstance(InstanceUtil.getContext(instance, true));
		getDocumentContext().setCurrentInstance(instance);
		List<SectionInstance> sections = instance.getSections();
		caseDocumentSections = DefinitionUtil.filterByPurpose(sections, null);
		removeNotPersistedDocuments(caseDocumentSections);
	}

	/**
	 * Removes the not persisted documents.
	 * 
	 * @param caseDocumentSections
	 *            the case document sections
	 */
	private void removeNotPersistedDocuments(List<SectionInstance> caseDocumentSections) {
		for (SectionInstance sectionInstance : caseDocumentSections) {
			ListIterator<Instance> listIterator = sectionInstance.getContent().listIterator();
			for (Iterator iterator = listIterator; iterator.hasNext();) {
				Instance instance = (Instance) iterator.next();
				if (InstanceUtil.isNotPersisted(instance)) {
					listIterator.remove();
				}
			}
		}
	}

	/**
	 * History open case documents tab page observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void historyOpenCaseDocumentsTabObserver(
			@Observes @NavigationHistoryType(NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS) NavigationHistoryEvent event) {
		log.debug("CMFWeb: Executing observer SectionDocumentsAction.historyOpenCaseDocumentsTabObserver");
		loadSectionDocuments();
	}

	/**
	 * Attach document observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void attachDocument(
			@Observes @EMFAction(value = ActionTypeConstants.ATTACH_DOCUMENT, target = SectionInstance.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer SectionDocumentsAction.attachDocument");
		getDocumentContext().addInstance(event.getInstance());
	}

	/**
	 * Getter method for caseDocumentSections.
	 * 
	 * @return the caseDocumentSections
	 */
	public List<SectionInstance> getCaseDocumentSections() {
		return caseDocumentSections;
	}

	/**
	 * Setter method for caseDocumentSections.
	 * 
	 * @param caseDocumentSections
	 *            the caseDocumentSections to set
	 */
	public void setCaseDocumentSections(List<SectionInstance> caseDocumentSections) {
		this.caseDocumentSections = caseDocumentSections;
	}

}
