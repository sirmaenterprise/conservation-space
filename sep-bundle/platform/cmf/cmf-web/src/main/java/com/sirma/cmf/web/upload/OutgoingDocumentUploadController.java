package com.sirma.cmf.web.upload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.workflow.task.TaskDocument;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOpenEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOpenEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.util.CurrentLocale;

/**
 * The Class OutgoingDocumentUploadController. Handles the operations of outgoing task documents
 * control.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class OutgoingDocumentUploadController extends EntityAction implements Serializable {

	private static final long serialVersionUID = -8871241856742974279L;

	/** The current locale producer. */
	@Inject
	@CurrentLocale
	private String currentLocale;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The outgoing documents for the task. */
	private List<TaskDocument> outgoingDocuments;

	/** The processed links. */
	@Inject
	private OutgoingDocumentsFlags outgoingDocumentsFlags;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/**
	 * Updates the instances after file upload is finished and links between uploaded documents and
	 * current task are created.
	 */
	public void updateModel() {
		// refresh case instance if exists
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if ((caseInstance != null) && SequenceEntityGenerator.isPersisted(caseInstance)) {
			caseInstanceService.refresh(caseInstance);
		}
		getDocumentContext().addInstance(caseInstance);

		// refresh task instance
		AbstractTaskInstance currentInstance = (AbstractTaskInstance) getDocumentContext()
				.getCurrentInstance();
		if (currentInstance != null) {
			currentInstance = taskService.loadByDbId(currentInstance.getId());
		}
		
		getDocumentContext().addInstance(currentInstance);

		// !!! fire task open event to allow injection
		if (currentInstance instanceof StandaloneTaskInstance) {
			eventService
					.fire(new StandaloneTaskOpenEvent((StandaloneTaskInstance) currentInstance));
		} else if (currentInstance instanceof TaskInstance) {
			eventService.fire(new TaskOpenEvent((TaskInstance) currentInstance));
		}
		// force reset outgoing documents
		outgoingDocumentsFlags.setProcessedLinks(false);
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
	 * Load outgoing documents.
	 * 
	 * @return A list with TaskDocument instances initialized to be used by the underlying table.
	 */
	@SuppressWarnings("unchecked")
	public List<TaskDocument> getOutgoingDocuments() {

		UIComponent currentComponent = UIComponent.getCurrentComponent(FacesContext
				.getCurrentInstance());

		if (!outgoingDocumentsFlags.isProcessedLinks()
				&& (currentComponent instanceof HtmlDataTable)) {
			Map<String, Object> attributes = currentComponent.getAttributes();
			Integer codelist = (Integer) attributes.get("codelist");
			List<TaskDocument> outgoingDocs = new ArrayList<TaskDocument>();
			// if we does not have a codelist we are not going to display anything
			if (codelist != null) {
				List<String> filtersList = (List<String>) attributes.get("filters");
				String[] filtersArr = filtersList.toArray(new String[filtersList.size()]);

				// a filter is provided in definition that should filter the document types codelist
				Map<String, CodeValue> filteredValues = codelistService.getFilteredCodeValues(
						codelist, filtersArr);

				// we should allow to upload only documents that are allowed to be uploaded into
				// the current case
				Map<String, Integer> allowedDocumentsForUpload = filterDefinedDocumentsForUpload(filteredValues
						.keySet());

				// find linked documents to current task and store for later use
				Instance task = getDocumentContext().getCurrentInstance();
				List<LinkInstance> links = Collections.EMPTY_LIST;
				if (task != null) {
					// get all outgoing document links for current task
					List<LinkReference> source = linkService.getLinks(task.toReference(),
							LinkConstants.OUTGOING_DOCUMENTS_LINK_ID);
					links = linkService.convertToLinkInstance(source, true);
				}
				Set<DocumentInstance> linkedDocuments = new LinkedHashSet<DocumentInstance>();
				for (LinkInstance linkInstance : links) {
					if (linkInstance.getTo() instanceof DocumentInstance) {
						DocumentInstance documentInstance = (DocumentInstance) linkInstance.getTo();
						linkedDocuments.add(documentInstance);
						String type = documentInstance.getIdentifier();
						// update the total count
						decrementCount(allowedDocumentsForUpload, type);
						CodeValue codeValue = filteredValues.get(type);
						// should not happen
						if (codeValue != null) {
							TaskDocument taskDocument = new TaskDocument();
							taskDocument.setDocumentType(type);
							taskDocument.setDocumentInstance(documentInstance);
							taskDocument.setUploaded(true);
							String clDocumentTitle = (String) filteredValues.get(type)
									.getProperties().get(currentLocale);
							taskDocument.setDocumentTypeDescription(clDocumentTitle);
							outgoingDocs.add(taskDocument);
						}
					}
				}

				// find documents that are uploaded and not linked, yet.
				List<DocumentInstance> attachedDocuments = findAttachedDocuments(filteredValues
						.keySet());
				if (!attachedDocuments.isEmpty()) {
					AbstractTaskInstance taskInstance = getContextTaskInstance();
					for (DocumentInstance documentInstance : attachedDocuments) {
						if (linkedDocuments.contains(documentInstance)) {
							continue;
						}
						linkedDocuments.add(documentInstance);
						String type = (String) documentInstance.getProperties().get(
								DocumentProperties.TYPE);
						// update the total count
						decrementCount(allowedDocumentsForUpload, type);
						TaskDocument taskDocument = new TaskDocument();
						taskDocument.setDocumentType(type);
						taskDocument.setDocumentInstance(documentInstance);
						taskDocument.setUploaded(true);
						String clDocumentTitle = (String) filteredValues.get(type).getProperties()
								.get(currentLocale);
						taskDocument.setDocumentTypeDescription(clDocumentTitle);
						outgoingDocs.add(taskDocument);

						// link the document to the task
						linkService.link(taskInstance, documentInstance,
								LinkConstants.OUTGOING_DOCUMENTS_LINK_ID,
								LinkConstants.OUTGOING_DOCUMENTS_LINK_ID,
								LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
					}
				}

				if (!allowedDocumentsForUpload.isEmpty()) {
					// show rows for document upload only for those that are allowed for upload
					filteredValues.keySet().retainAll(allowedDocumentsForUpload.keySet());
					// remove already uploaded documents
					allowedDocumentsForUpload.keySet().retainAll(filteredValues.keySet());

					// create empty rows
					for (Entry<String, CodeValue> key : filteredValues.entrySet()) {
						TaskDocument taskDocument = new TaskDocument();
						outgoingDocs.add(initTaskDocument(key.getValue(), key.getKey(),
								taskDocument));
					}
				}
			}
			outgoingDocuments = outgoingDocs;
			outgoingDocumentsFlags.setProcessedLinks(true);
		}
		return outgoingDocuments;
	}

	/**
	 * Decrement count.
	 * 
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 */
	private void decrementCount(Map<String, Integer> map, String key) {
		Integer integer = map.get(key);
		if ((integer != null) && (integer > 0)) {
			int value = integer - 1;
			if (value == 0) {
				// if reached the maximum count remove it from the map
				map.remove(key);
			} else {
				map.put(key, value);
			}
		}
		// if the value is -1 we does not need to update it it points to infinity
	}

	/**
	 * Get allowed documents for upload from the current case that are part of the given filter.
	 * 
	 * @param keySet
	 *            the key set
	 * @return a mapping with the allowed documents and the number of allowed instances
	 */
	private Map<String, Integer> filterDefinedDocumentsForUpload(Set<String> keySet) {
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		Map<String, Integer> all = new LinkedHashMap<String, Integer>();
		Map<String, Map<String, Integer>> definedDocuments = documentService
				.getDefinedDocuments(caseInstance);
		for (Map<String, Integer> map : definedDocuments.values()) {
			for (Entry<String, Integer> entry : map.entrySet()) {
				if (keySet.contains(entry.getKey())) {
					Integer integer = all.get(entry.getKey());
					if ((integer == null) && (entry.getValue() > 0)) {
						all.put(entry.getKey(), entry.getValue());
					} else {
						if (integer == null) {
							integer = 0;
						}
						if (entry.getValue() < 0) {
							// the document will be allowed to be uploaded only once
							all.put(entry.getKey(), -1);
						} else {
							all.put(entry.getKey(), integer + entry.getValue());
						}
					}
				}
			}
		}
		return all;
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
		List<DocumentInstance> documents = new LinkedList<DocumentInstance>();
		if (caseInstance != null) {
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
		}
		return documents;
	}

	/**
	 * Create a TaskDocument for not uploaded document instances. Such TaskDocuments would be
	 * displayed in the outgoing task documents table as not uploaded yet and would have upload
	 * button next to them.
	 * 
	 * @param codeValue
	 *            the code value
	 * @param type
	 *            the type
	 * @param taskDocument
	 *            the task document
	 * @return the task document
	 */
	private TaskDocument initTaskDocument(CodeValue codeValue, String type,
			TaskDocument taskDocument) {
		taskDocument.setUploaded(false);
		String clDocumentTitle = (String) codeValue.getProperties().get(currentLocale);
		taskDocument.setDocumentTypeDescription(clDocumentTitle);
		taskDocument.setDocumentType(type);
		return taskDocument;
	}

	/**
	 * Setter method for outgoingDocuments.
	 * 
	 * @param outgoingDocuments
	 *            the outgoingDocuments to set
	 */
	public void setOutgoingDocuments(List<TaskDocument> outgoingDocuments) {
		this.outgoingDocuments = outgoingDocuments;
	}

	/**
	 * Getter method for processedLinks.
	 * 
	 * @return the processedLinks
	 */
	public boolean isProcessedLinks() {
		return outgoingDocumentsFlags.isProcessedLinks();
	}

	/**
	 * Setter method for processedLinks.
	 * 
	 * @param processedLinks
	 *            the processedLinks to set
	 */
	public void setProcessedLinks(boolean processedLinks) {
		outgoingDocumentsFlags.setProcessedLinks(processedLinks);
	}

}
