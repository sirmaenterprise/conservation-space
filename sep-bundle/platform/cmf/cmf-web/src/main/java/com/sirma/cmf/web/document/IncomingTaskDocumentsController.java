package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.workflow.task.TaskDocument;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * The Class IncomingTaskDocumentsController handles actions from incoming task documents control.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class IncomingTaskDocumentsController extends EntityAction implements Serializable {

	private static final long serialVersionUID = -5939566760917713064L;

	@Inject
	private LinkService linkService;

	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

	private List<TaskDocument> incomingDocuments;

	private List<DocumentInstance> selectedDocuments;

	@Inject
	private IncomingDocumentsFlags incomingDocumentsFlags;

	private List<SectionInstance> allowedSections;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		allowedSections = new ArrayList<>();
		initAllowedSections();
	}

	/**
	 * Inits the allowed sections.
	 */
	private void initAllowedSections() {
		allowedSections.clear();
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (caseInstance != null) {
			instanceService.refresh(caseInstance);
			List<SectionInstance> sections = caseInstance.getSections();
			for (SectionInstance sectionInstance : sections) {
				boolean hasVisibleDocuments = hasVisibleDocuments(sectionInstance);
				if (StringUtils.isNullOrEmpty(sectionInstance.getPurpose()) && hasVisibleDocuments) {
					allowedSections.add(sectionInstance);
				}
			}
		}
	}

	/**
	 * Checks for visible documents.
	 * 
	 * @param sectionInstance
	 *            the section instance
	 * @return true, if successful
	 */
	protected boolean hasVisibleDocuments(SectionInstance sectionInstance) {
		List<Instance> content = sectionInstance.getContent();
		List<TaskDocument> incomingDocumentsList = getCachedIncomingDocuments();
		for (Instance instance : content) {
			boolean isLinked = isLinked(incomingDocumentsList, instance.getId());
			if (!isLinked) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Opens a selected document instance. Selected document instance is taken from a link instance
	 * and is dereferenced. So the actual document instance should be found from the current case.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @return the string
	 */
	public String open(DocumentInstance selectedDocumentInstance) {
		if (selectedDocumentInstance == null) {
			return null;
		}
		String dmsId = selectedDocumentInstance.getDmsId();
		DocumentInstance actualDocumentInstance = findDocumentInCase(dmsId);
		if (actualDocumentInstance != null) {
			getDocumentContext().setDocumentInstance(actualDocumentInstance);
			return caseDocumentsTableAction.open(actualDocumentInstance);
		}
		return null;
	}

	/**
	 * Show available documents.
	 */
	public void showAvailableDocuments() {
		log.debug("CMFWeb: Executing IncomingTaskDocumentsController.showAvailableDocuments");
		if (selectedDocuments == null) {
			selectedDocuments = new ArrayList<DocumentInstance>();
		} else {
			selectedDocuments.clear();
		}
		initAllowedSections();
	}

	/**
	 * Gets the css row classes for case sections list.
	 * 
	 * @return the css row classes
	 */
	public String getRowClasses() {
		StringBuilder rowClasses = new StringBuilder();
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (caseInstance != null) {
			List<SectionInstance> sections = caseInstance.getSections();
			for (SectionInstance sectionInstance : sections) {
				if (sectionInstance.getContent().isEmpty()) {
					rowClasses.append(",pad-0 empty-row");
					continue;
				}
				rowClasses.append(",pad-0");
			}
		}
		return rowClasses.toString();
	}

	/**
	 * Checks if a document with given id is already linked.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return true, if is linked
	 */
	public boolean isLinked(String dmsId) {
		return checkIsLinked(getIncomingDocuments(), dmsId);
	}

	/**
	 * Checks if a document with given id is already linked.
	 * 
	 * @param incomingDocumentsList
	 *            the incoming documents list
	 * @param dbId
	 *            the db id
	 * @return true, if is linked
	 */
	public boolean isLinked(List<TaskDocument> incomingDocumentsList, Serializable dbId) {
		return checkIsLinked(incomingDocumentsList, dbId);
	}

	/**
	 * Check if document with given id is linked to the task.
	 * 
	 * @param incomingDocumentsList
	 *            the incoming documents
	 * @param dbId
	 *            the db id
	 * @return true, if successful
	 */
	private boolean checkIsLinked(List<TaskDocument> incomingDocumentsList, Serializable dbId) {
		boolean linked = false;
		if (incomingDocumentsList != null) {
			for (TaskDocument taskDocument : incomingDocumentsList) {
				if (EqualsHelper.nullSafeEquals(taskDocument.getDocumentInstance().getId(), dbId)) {
					linked = true;
					break;
				}
			}
		}
		return linked;
	}

	/**
	 * Update selected documents list. The list contains all the selected documents.
	 * 
	 * @param selectedDocument
	 *            the selected document
	 */
	public void updateSelection(DocumentInstance selectedDocument) {

		if (selectedDocument == null) {
			return;
		}

		if (selectedDocuments != null) {
			int indexOf = selectedDocuments.indexOf(selectedDocument);
			if (indexOf < 0) {
				selectedDocuments.add(selectedDocument);
			} else {
				selectedDocuments.remove(indexOf);
			}
		}
	}

	/**
	 * Checks for uploaded documents and for every uploaded document check if is already linked.
	 * Used from the button that opens a panel for adding of incoming documents.
	 * 
	 * @return true, if successful
	 */
	public boolean hasUploadedDocuments() {
		boolean hasUploadedDocuments = false;

		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (caseInstance == null) {
			return hasUploadedDocuments;
		}

		List<SectionInstance> sections = caseInstance.getSections();
		for (SectionInstance sectionInstance : sections) {
			List<Instance> documents = sectionInstance.getContent();
			boolean hasDocuments = !documents.isEmpty();
			boolean hasLinkedDocuments = false;

			for (Instance documentInstance : documents) {
				if (documentInstance instanceof DocumentInstance) {
					boolean isLinked = isLinked(((DocumentInstance) documentInstance).getDmsId());
					if (isLinked) {
						hasLinkedDocuments = true;
						break;
					}
				}
			}

			if (hasDocuments && hasLinkedDocuments) {
				hasUploadedDocuments = true;
				break;
			}
		}

		return hasUploadedDocuments;
	}

	/**
	 * Adds the incoming document to taskDocuments list and call service to make a link between the
	 * current task and every of selected documents instances.
	 */
	public void addIncomingDocuments() {
		AbstractTaskInstance taskInstance = getContextTaskInstance();
		for (DocumentInstance documentInstance : selectedDocuments) {
			TaskDocument taskDocument = new TaskDocument();
			taskDocument.setDocumentInstance(documentInstance);
			taskDocument.setAttached(true);
			taskDocument.setDocumentType("document type");
			incomingDocuments.add(taskDocument);

			linkService.link(taskInstance, documentInstance,
					LinkConstants.INCOMING_DOCUMENTS_LINK_ID,
					LinkConstants.INCOMING_DOCUMENTS_LINK_ID,
					LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
		}
		initAllowedSections();
	}

	/**
	 * Gets the cached incoming documents.
	 * Used to avoid cyclic invocation and concurrent modification of allowedSections list.
	 * 
	 * @return the cached incoming documents
	 */
	public List<TaskDocument> getCachedIncomingDocuments() {
		return incomingDocuments;
	}

	/**
	 * Gets the incoming documents from link service. Put all the linked documents in the incoming
	 * documents list.
	 * 
	 * @return the incoming documents
	 */
	@SuppressWarnings("unchecked")
	public List<TaskDocument> getIncomingDocuments() {
		if (incomingDocumentsFlags.isProcessedLinks()) {
			return incomingDocuments;
		}
		// have wrapper for the attach button, so we will try to find the child table
		UIComponent currentComponent = UIComponent.getCurrentComponent(FacesContext
				.getCurrentInstance());
		if (!(currentComponent instanceof HtmlDataTable)) {
			if (currentComponent != null) {
				List<UIComponent> children = currentComponent.getChildren();
				for (UIComponent uiComponent : children) {
					if (uiComponent instanceof HtmlDataTable) {
						currentComponent = uiComponent;
						break;
					}
				}
			} else {
				if (incomingDocuments == null) {
					incomingDocuments = new LinkedList<>();
				}
				return incomingDocuments;
			}
		}

		if (!incomingDocumentsFlags.isProcessedLinks()
				&& (currentComponent instanceof HtmlDataTable)) {
			AbstractTaskInstance instance = getContextTaskInstance();
			List<LinkInstance> links = Collections.EMPTY_LIST;
			if (instance != null) {
				links = linkService.convertToLinkInstance(linkService.getLinks(
						instance.toReference(), LinkConstants.INCOMING_DOCUMENTS_LINK_ID), true);
			}

			List<TaskDocument> incomingDocumentsList = new LinkedList<TaskDocument>();

			// add the existing links at the beginning of the list
			for (LinkInstance linkInstance : links) {
				if (linkInstance.getTo() instanceof DocumentInstance) {
					DocumentInstance incomingDocument = (DocumentInstance) linkInstance.getTo();
					TaskDocument taskDocument = new TaskDocument();
					taskDocument.setAttached(true);
					taskDocument.setDocumentInstance(incomingDocument);
					taskDocument.setDocumentType("doctype");
					incomingDocumentsList.add(taskDocument);
				}
			}

			incomingDocuments = incomingDocumentsList;

			// check if we have configured the additional filters
			Map<String, Object> attributes = currentComponent.getAttributes();
			Integer codelist = (Integer) attributes.get("codelist");
			if (codelist != null) {
				List<String> filtersList = (List<String>) attributes.get("filters");
				String[] filtersArr = filtersList.toArray(new String[filtersList.size()]);

				// a filter is provided in definition that should filter the document types codelist
				Map<String, CodeValue> filteredValues = codelistService.getFilteredCodeValues(
						codelist, filtersArr);

				// remove documents that are already linked
				for (LinkInstance linkInstance : links) {
					if (linkInstance.getTo() != null) {
						filteredValues.remove(linkInstance.getTo().getProperties()
								.get(DocumentProperties.TYPE));
					}
				}
				// find documents that are uploaded and not linked, yet.
				List<DocumentInstance> attachedDocuments = findAttachedDocuments(filteredValues
						.keySet());

				if (!attachedDocuments.isEmpty()) {
					// attach the already uploaded documents
					List<DocumentInstance> oldSelection = selectedDocuments;
					selectedDocuments = attachedDocuments;
					addIncomingDocuments();
					selectedDocuments = oldSelection;
				}
			}

			incomingDocumentsFlags.setProcessedLinks(true);
		}

		return incomingDocuments;
	}

	/**
	 * Find attached documents that have property value for key {@link DocumentProperties#TYPE} that
	 * corresponds to the given set of document types.
	 * 
	 * @param keySet
	 *            the key set to check against
	 * @return the list if found documents that match the filter
	 */
	private List<DocumentInstance> findAttachedDocuments(Set<String> keySet) {
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (caseInstance == null) {
			if (getDocumentContext().getContextInstance() instanceof CaseInstance) {
				caseInstance = (CaseInstance) getDocumentContext().getContextInstance();
			} else {
				caseInstance = InstanceUtil.getParent(CaseInstance.class, getDocumentContext()
						.getContextInstance());
			}
		}
		if (caseInstance == null) {
			return CollectionUtils.emptyList();
		}
		List<DocumentInstance> documents = new LinkedList<DocumentInstance>();
		for (SectionInstance sectionInstance : caseInstance.getSections()) {
			for (Instance documentInstance : sectionInstance.getContent()) {
				if (documentInstance instanceof DocumentInstance) {
					Serializable serializable = documentInstance.getProperties().get(
							DocumentProperties.TYPE);
					if ((serializable != null) && keySet.contains(serializable)) {
						documents.add((DocumentInstance) documentInstance);
					}
				}
			}
		}
		return documents;
	}

	/**
	 * Removes the incoming document. Called by the remove document button from incoming documents
	 * table.
	 * 
	 * @param taskDocument
	 *            the task document
	 */
	public void removeIncomingDocument(TaskDocument taskDocument) {
		if ((taskDocument == null) || (taskDocument.getDocumentInstance() == null)) {
			return;
		}

		DocumentInstance documentInstance = taskDocument.getDocumentInstance();
		int indexOf = incomingDocuments.indexOf(taskDocument);
		AbstractTaskInstance instance = getContextTaskInstance();
		if ((indexOf >= 0) && (instance != null) && (documentInstance != null)) {
			linkService.unlink(instance.toReference(), documentInstance.toReference(),
					LinkConstants.INCOMING_DOCUMENTS_LINK_ID,
					LinkConstants.INCOMING_DOCUMENTS_LINK_ID);

			incomingDocuments.remove(indexOf);
		}
		initAllowedSections();
	}

	/**
	 * Gets the context task instance.
	 * 
	 * @return the context task instance
	 */
	private AbstractTaskInstance getContextTaskInstance() {
		AbstractTaskInstance instance = getDocumentContext().getInstance(TaskInstance.class);
		if (instance == null) {
			instance = getDocumentContext().getInstance(StandaloneTaskInstance.class);
		}
		return instance;
	}

	/**
	 * Finds a document in current case by dmsid.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the document instance
	 */
	protected DocumentInstance findDocumentInCase(String dmsId) {
		DocumentInstance foundDocument = null;
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if (caseInstance != null) {
			List<SectionInstance> sections = caseInstance.getSections();
			for (SectionInstance sectionInstance : sections) {
				boolean found = false;
				List<Instance> documents = sectionInstance.getContent();
				for (Instance documentInstance : documents) {
					if ((documentInstance instanceof DocumentInstance)
							&& EqualsHelper.nullSafeEquals(
									((DocumentInstance) documentInstance).getDmsId(), dmsId)) {
						foundDocument = (DocumentInstance) documentInstance;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return foundDocument;
	}

	/**
	 * Setter method for incomingDocuments.
	 * 
	 * @param incomingDocuments
	 *            the incomingDocuments to set
	 */
	public void setIncomingDocuments(List<TaskDocument> incomingDocuments) {
		this.incomingDocuments = incomingDocuments;
	}

	/**
	 * Getter method for selectedDocuments.
	 * 
	 * @return the selectedDocuments
	 */
	public List<DocumentInstance> getSelectedDocuments() {
		return selectedDocuments;
	}

	/**
	 * Setter method for selectedDocuments.
	 * 
	 * @param selectedDocuments
	 *            the selectedDocuments to set
	 */
	public void setSelectedDocuments(List<DocumentInstance> selectedDocuments) {
		this.selectedDocuments = selectedDocuments;
	}

	/**
	 * Getter method for allowedSections.
	 * 
	 * @return the allowedSections
	 */
	public List<SectionInstance> getAllowedSections() {
		return allowedSections;
	}

	/**
	 * Setter method for allowedSections.
	 * 
	 * @param allowedSections
	 *            the allowedSections to set
	 */
	public void setAllowedSections(List<SectionInstance> allowedSections) {
		this.allowedSections = allowedSections;
	}
}
