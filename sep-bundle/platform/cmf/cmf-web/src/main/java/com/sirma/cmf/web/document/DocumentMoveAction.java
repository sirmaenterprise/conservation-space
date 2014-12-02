/*
 * 
 */
package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityPreviewAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.EntityOpenDispatcher;
import com.sirma.cmf.web.search.modal.AbstractBrowserHandler;
import com.sirma.cmf.web.search.modal.EntityBrowserHandler;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.CMInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * DocumentMoveAction responsible for operations in document move panel.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentMoveAction extends
		AbstractBrowserHandler<SectionInstance, DocumentInstance, DocumentInstance> implements
		EntityBrowserHandler, Serializable {

	private static final long serialVersionUID = -913184520707409738L;

	@Inject
	private EntityPreviewAction entityPreviewAction;

	private DocumentInstance selectedDocumentForMove;

	private String selectedTargetSectionId;

	private SectionInstance selectedSectionInstance;

	private Action action;

	@Inject
	private SectionService sectionService;

	@Inject
	private EntityOpenDispatcher entityOpenDispatcher;
	
	/** The faces context. */
	@Inject
	private FacesContext facesContext;

	@Override
	public void toggleTarget(SectionInstance selectedSection) {
		if (selectedSection == null) {
			return;
		}
		if ((selectedSectionInstance != null)
				&& EqualsHelper.entityEquals(selectedSection, selectedSectionInstance)) {
			selectedSectionInstance = null;
			selectedTargetSectionId = null;
		} else {
			selectedSectionInstance = selectedSection;
			selectedTargetSectionId = selectedSection.getIdentifier();
		}
	}

	@Override
	public String acceptSelectedTarget() {
		return null;
	}
	
	/**
	 * Move to other case.
	 * 
	 * @return Navigation string.
	 */
	protected String moveToOtherCase() {
		
		String objectId = getParameterByName("id");
		String sectionId = getParameterByName("sectionId");
		String type = getParameterByName("type");
		
		String navigation = NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS;
			SectionInstance sectionInstance = selectedSectionInstance;
			if (selectedSectionInstance == null) {
				sectionInstance = sectionService.loadByDbId(sectionId);
			}
			if(selectedDocumentForMove == null) {
				selectedDocumentForMove = documentService.loadByDbId(objectId);
			}
			
			boolean moved = documentService.moveDocument(selectedDocumentForMove, sectionInstance);
			if (!moved) {
				// TODO show error message
				log.debug("CMFWeb: NOT Moved document ["
						+ selectedDocumentForMove.getDmsId()
						+ "] in case ["
						+ InstanceUtil.getParent(CMInstance.class, sectionInstance)
								.getContentManagementId() + "/" + sectionInstance.getIdentifier()
						+ "]");
			} else {
				log.debug("CMFWeb: moved document "
						+ selectedDocumentForMove.getId()
						+ " to case "
						+ InstanceUtil.getParent(CMInstance.class, sectionInstance)
								.getContentManagementId() + "/" + sectionInstance.getIdentifier());
				entityOpenDispatcher.openInternal(sectionInstance, null);
				// reloadCaseInstance();
			}
		
		return navigation;
	}

	/**
	 * Reload document instance.
	 */
	protected void reloadDocumentInstance() {
		String documentDmsId = selectedDocumentForMove.getDmsId();
		List<SectionInstance> sections = getDocumentContext().getInstance(CaseInstance.class)
				.getSections();
		boolean found = false;
		for (SectionInstance sectionInstance : sections) {
			List<Instance> documents = sectionInstance.getContent();
			for (Instance documentInstance : documents) {
				if ((documentInstance instanceof DocumentInstance)
						&& documentDmsId.equals(((DocumentInstance) documentInstance).getDmsId())) {
					getDocumentContext().setDocumentInstance((DocumentInstance) documentInstance);
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}
	}

	@Override
	public void initialize(DocumentInstance documentInstance) {
		log.debug("CMFWeb: DocumentMoveAction initializing document move handler for document: "
				+ documentInstance.getId() + " with identifier " + documentInstance.getIdentifier());
		if(documentInstance.getOwningInstance() == null){
			documentInstance = documentService.loadByDbId(documentInstance.getId());
			getDocumentContext().setDocumentInstance(documentInstance);
		}
		Instance contextCaseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (contextCaseInstance == null) {
			Instance caseInstance = InstanceUtil.getParentContext(documentInstance, true);
			if (caseInstance != null) {
				getDocumentContext().addInstance(caseInstance);
			}
		}
		selectedDocumentForMove = documentInstance;
		selectedTargetSectionId = documentInstance.getOwningInstance().getIdentifier();
		selectedSectionInstance = null;
	}

	@Override
	public boolean isCurrent(SectionInstance sectionInstance) {
		if (sectionInstance != null) {
			return sectionInstance.getIdentifier().equals(selectedTargetSectionId);
		}
		return false;
	}

	@Override
	public boolean canHandle(Action action) {
		if (action != null) {
			String actionId = action.getActionId();
			boolean canHandle = ActionTypeConstants.MOVE_SAME_CASE.equals(actionId)
					|| ActionTypeConstants.MOVE_OTHER_CASE.equals(actionId);
			if (canHandle) {
				this.action = action;
			}
			return canHandle;
		}
		return false;
	}

	@Override
	public <S extends SearchArguments<Instance>> void afterSearch(S searchData) {
		String currentCaseId = (String) getDocumentContext().getInstance(CaseInstance.class)
				.getId();
		// filter current case, not active cases and those that current user don't have access to
		// open
		Iterator<Instance> caseListIterator = searchData.getResult().iterator();
		for (Iterator<Instance> iterator = caseListIterator; iterator.hasNext();) {
			CaseInstance caseInstance = (CaseInstance) iterator.next();
			// changed checks to eliminate multiple removals of the same case from the list
			// CMF-1576
			if (caseInstance.getId().equals(currentCaseId) || !isCaseOpened(caseInstance)
					|| !entityPreviewAction.canEditCase(caseInstance)) {
				iterator.remove();
				continue;
			}
			// Remove sections that have different purpose than the default.
			// For example: the object sections should be removed in order to not allow the user to
			// move documents in such sections.
			List<SectionInstance> sections = caseInstance.getSections();
			for (Iterator<SectionInstance> sectionsIterator = sections.iterator(); sectionsIterator
					.hasNext();) {
				SectionInstance sectionInstance = sectionsIterator.next();
				if (StringUtils.isNotNullOrEmpty(sectionInstance.getPurpose())) {
					sectionsIterator.remove();
				}
			}
		}
	}

	/**
	 * Filter case sections to remain only the document sections - those without a purpose
	 * attribute.
	 * 
	 * @param caseInstance
	 *            the case instance
	 * @return filtered sections
	 */
	public List<SectionInstance> filterSections(CaseInstance caseInstance) {
		List<SectionInstance> sections = new ArrayList<SectionInstance>();
		if (caseInstance != null) {
			List<SectionInstance> availableSections = caseInstance.getSections();
			if (availableSections != null) {
				for (SectionInstance sectionInstance : availableSections) {
					if (StringUtils.isNullOrEmpty(sectionInstance.getPurpose())) {
						sections.add(sectionInstance);
					}
				}
			}
		}
		return sections;
	}

	@Override
	public String cancelSelection() {
		selectedSectionInstance = null;
		selectedDocumentForMove = null;
		return null;
	}

	/**
	 * Getter method for selectedSectionInstance.
	 * 
	 * @return the selectedSectionInstance
	 */
	public SectionInstance getSelectedSectionInstance() {
		return selectedSectionInstance;
	}

	/**
	 * Setter method for selectedSectionInstance.
	 * 
	 * @param selectedSectionInstance
	 *            the selectedSectionInstance to set
	 */
	public void setSelectedSectionInstance(SectionInstance selectedSectionInstance) {
		this.selectedSectionInstance = selectedSectionInstance;
	}

	/**
	 * Getter method for selectedDocumentForMove.
	 * 
	 * @return the selectedDocumentForMove
	 */
	public DocumentInstance getSelectedDocumentForMove() {
		return selectedDocumentForMove;
	}

	/**
	 * Setter method for selectedDocumentForMove.
	 * 
	 * @param selectedDocumentForMove
	 *            the selectedDocumentForMove to set
	 */
	public void setSelectedDocumentForMove(DocumentInstance selectedDocumentForMove) {
		this.selectedDocumentForMove = selectedDocumentForMove;
	}

	/**
	 * Getter method for selectedTargetSectionId.
	 * 
	 * @return the selectedTargetSectionId
	 */
	public String getSelectedTargetSectionId() {
		return selectedTargetSectionId;
	}

	/**
	 * Setter method for selectedTargetSectionId.
	 * 
	 * @param selectedTargetSectionId
	 *            the selectedTargetSectionId to set
	 */
	public void setSelectedTargetSectionId(String selectedTargetSectionId) {
		this.selectedTargetSectionId = selectedTargetSectionId;
	}

	/**
	 * Getter method for action.
	 * 
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Setter method for action.
	 * 
	 * @param action
	 *            the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}
	
	/**
	 * Gets request parameter by name.
	 * 
	 * @param name
	 *            the name of the parameter
	 * @return parameter value
	 */
	protected String getParameterByName(String name) {
		Map<String, String> requestParameterMap = facesContext.getExternalContext()
				.getRequestParameterMap();
		return requestParameterMap.get(name);
	}

}
