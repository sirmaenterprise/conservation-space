package com.sirma.cmf.web.document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.caseinstance.CaseDocumentsTableAction;
import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.BuilderCssConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.navigation.NavigationHandlerConstants;
import com.sirma.itt.cmf.beans.ContentPreviewDescriptor;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.AuditableEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfPermission;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * Document instance processing manager.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentAction extends EntityAction implements Serializable {

	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = 942389475738177142L;

	/** IFrame <b>SRC</b> string. */
	private String iframeSource;

	/** Comment for documentHistoryVersion. */
	private boolean documentHistoryVersion;

	/** Available link source. */
	private String previewSourceType;

	/** The section instance id. */
	private String sectionInstanceId;

	/** The selected document for move. */
	private DocumentInstance selectedDocumentForMove;

	/** The image URL version suffix. */
	private static final String IMAGE_URL_VERSION_SUFFIX = "?v=";

	/** The case documents table action. */
	@Inject
	private CaseDocumentsTableAction caseDocumentsTableAction;

	/** The document role evaluator. */
	@Inject
	@RoleEvaluatorType(ObjectTypesCmf.DOCUMENT)
	private RoleEvaluator<DocumentInstance> documentRoleEvaluator;

	/** The document preview path. */
	private String documentPreviewPath;

	/**
	 * Initialize the document preview URI on post construct.
	 */
	@PostConstruct
	public void initialize() {
		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();
		if (StringUtils.isNullOrEmpty(documentPreviewPath)) {
			documentPreviewPath = getDocumentPreviewURL(documentInstance);
		}
	}

	/**
	 * Retrieve document location path, based on document instance.
	 * 
	 * @param documentInstance
	 *            current document instance
	 * @return document path
	 */
	// TODO: maybe this should become a rest service and to be called only when requested download
	public String getDocumentDownloadUrl(Instance documentInstance) {
		String documentPath = null;
		if (documentInstance instanceof DocumentInstance) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext == null) {
				return null;
			}
			DocumentInstance loadedDocumentInstance = documentService.loadByDbId(documentInstance
					.getId());
			documentPath = facesContext.getExternalContext().getRequestContextPath();
			documentPath = documentPath + documentService.getContentURI(loadedDocumentInstance);
		}
		return documentPath;
	}

	/**
	 * Register download action by firing an auditable event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void registerDownloadAction(Instance instance) {
		if (instance instanceof DocumentInstance) {
			AuditableEvent event = new AuditableEvent(instance, ActionTypeConstants.DOWNLOAD);
			eventService.fire(event);
		}
	}

	/**
	 * Observer for editing document properties.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void editDocumentProperties(
			@Observes @EMFAction(value = ActionTypeConstants.EDIT_DETAILS, target = DocumentInstance.class) final EMFActionEvent event) {
		instanceService.refresh(event.getInstance());
		Instance rootInstance = InstanceUtil.getRootInstance(event.getInstance(), true);
		getDocumentContext().addInstance(rootInstance);
		getDocumentContext().setRootInstance(rootInstance);
		log.debug("Executing observer DocumentAction.editDocumentProperties");
		DocumentInstance sectionDocument = (DocumentInstance) event.getInstance();
		getDocumentContext().setDocumentInstance(sectionDocument);
		DocumentDefinitionRef documentDefinition = (DocumentDefinitionRef) dictionaryService
				.getInstanceDefinition(sectionDocument);
		getDocumentContext().put(DocumentContext.DOCUMENT_DEFINITION, documentDefinition);
		event.setNavigation(NavigationConstants.NAVIGATE_DOCUMENT_EDIT_PROPERTIES);
	}

	/**
	 * Save document properties.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @return Navigation string.
	 */
	public String saveDocumentProperties(DocumentInstance documentInstance) {
		log.debug("Executing DocumentAction.saveDocumentProperties");
		if (documentInstance != null) {
			documentService.updateProperties(documentInstance);
		}
		return NavigationConstants.BACKWARD;
	}

	/**
	 * Cancel saving.
	 * 
	 * @return Navigation string.
	 */
	public String cancelEdit() {
		log.debug("Executing observer DocumentAction.cancelEdit for document properties");
		return NavigationConstants.BACKWARD;
	}

	/**
	 * Initializes the document properties form for edit.
	 */
	public void initDocumentPropertiesForm() {
		log.debug("DocumentAction.initDocumentPropertiesForm for editing");
		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();
		DocumentDefinitionRef documentDefinition = (DocumentDefinitionRef) getDocumentContext()
				.get(DocumentContext.DOCUMENT_DEFINITION);
		if ((documentInstance != null) && (documentDefinition != null)) {
			UIComponent panel = getFormPanel();
			// Existing case should be rendered in edit mode.
			if (SequenceEntityGenerator.isPersisted(documentInstance)) {
				if (panel != null) {
					panel.getChildren().clear();
				}
				invokeReader(documentDefinition, documentInstance, panel, FormViewMode.EDIT, null);
			}
		}
	}

	/**
	 * Gets the form panel where the definition data to be displayed. This is separated for
	 * testability.
	 * 
	 * @return the form panel
	 */
	protected UIComponent getFormPanel() {
		return RichFunction.findComponent(CMFConstants.DOCUMENT_PROPERTIES_PANEL);
	}

	/**
	 * Edit offline.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void editOffline(
			@Observes @EMFAction(value = ActionTypeConstants.EDIT_OFFLINE, target = DocumentInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.editOffline - checkout and lock the document");
		DocumentInstance documentInstance = (DocumentInstance) event.getInstance();
		documentService.checkOut(documentInstance);
		event.setNavigation(NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Edits the online.
	 * 
	 * @param event
	 *            the event
	 */
	public void editOnline(
			@Observes @EMFAction(value = ActionTypeConstants.EDIT_ONLINE, target = DocumentInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.editOffline - checkout and lock the document");
		DocumentInstance documentInstance = (DocumentInstance) event.getInstance();
		event.setNavigation(caseDocumentsTableAction.openForEdit(documentInstance));
	}

	/**
	 * Create sub document.
	 * 
	 * @param event
	 *            the event
	 */
	public void createSubDocument(
			@Observes @EMFAction(value = ActionTypeConstants.CREATE_SUB_DOCUMENT, target = DocumentInstance.class) final EMFActionEvent event) {
		DocumentInstance documentInstance = (DocumentInstance) event.getInstance();
		event.setNavigation(caseDocumentsTableAction.openForEdit(documentInstance));
	}

	/**
	 * Save as public template.
	 * 
	 * @param event
	 *            the event
	 */
	public void saveAsTemplate(
			@Observes @EMFAction(value = ActionTypeConstants.SAVE_AS_PUBLIC_TEMPLATE, target = DocumentInstance.class) final EMFActionEvent event) {
		DocumentInstance documentInstance = (DocumentInstance) event.getInstance();
		event.setNavigation(caseDocumentsTableAction.openForEdit(documentInstance));
	}

	/**
	 * Delete document.
	 * <ul>
	 * <li>If deleted from user dashboard, then document instance should be removed from context.</li>
	 * <li>If deleted from project dashboard, then document instance should be removed from context
	 * and project should be set as current instance instead.</li>
	 * <li>If deleted from case dashboard, then document instance should be removed from context and
	 * case should be set as current instance instead.</li>
	 * <li>If deleted from case documents tab, then document instance should be removed from context
	 * and the case should be set instead.</li>
	 * <li>If deleted from document landing page, then document instance should be removed from
	 * context and the parent context: case or project or object or document should be set instead.</li>
	 * </ul>
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void delete(
			@Observes @EMFAction(value = ActionTypeConstants.DELETE, target = DocumentInstance.class) final EMFActionEvent event) {
		DocumentInstance documentInstance = (DocumentInstance) event.getInstance();
		log.debug("Executing observer DocumentAction.delete - delete document "
				+ documentInstance.getProperties());

		// get the parent context and if it appears to be section instance, then get the next parent
		// instance in order to populate the document context after delete
		Instance parentContext = getDocumentContext().getContextInstance();
		if (parentContext instanceof SectionInstance) {
			parentContext = ((OwnedModel) parentContext).getOwningInstance();
		}

		boolean isDocumentOpened = getDocumentContext().getDocumentInstance() != null;

		boolean deleted = documentService.deleteDocument(documentInstance);

		// clear the document instance from context after delete
		if (deleted) {
			getDocumentContext().removeDocumentData();
			// populate context with parent instance of the deleted document
			if (parentContext != null) {
				getDocumentContext().setCurrentInstance(parentContext);
			}
		}

		// determine navigation string:
		// - if we have document instance in context, assume we are on document landing page and
		// after delete we should return to previous visited page
		// - otherwise we should reload current page
		String navigationString = NavigationHandlerConstants.BACKWARD;
		if (!isDocumentOpened) {
			navigationString = NavigationConstants.RELOAD_PAGE;
		}
		event.setNavigation(navigationString);
	}

	/**
	 * Download document action observer.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void uploadNewVersion(
			@Observes @EMFAction(value = ActionTypeConstants.UPLOAD_NEW_VERSION, target = DocumentInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.uploadNewVersion");
		event.setNavigation(NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Cancel document edit and unlocks it.
	 * 
	 * @param event
	 *            Event payload object
	 */
	public void cancelDocumentEdit(
			@Observes @EMFAction(value = ActionTypeConstants.STOP_EDIT, target = DocumentInstance.class) EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.CancelDocumentEdit");
		documentService.cancelCheckOut((DocumentInstance) event.getInstance());
		event.setNavigation(NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Locks document.
	 * 
	 * @param event
	 *            Event payload object
	 */
	public void documentLock(
			@Observes @EMFAction(value = ActionTypeConstants.LOCK, target = DocumentInstance.class) EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.documentLock");
		documentService.lock((DocumentInstance) event.getInstance());
	}

	/**
	 * UnLocks a locked document.
	 * 
	 * @param event
	 *            Event payload object
	 */
	public void documentUnlock(
			@Observes @EMFAction(value = ActionTypeConstants.UNLOCK, target = DocumentInstance.class) EMFActionEvent event) {
		log.debug("Executing observer DocumentAction.documentUnlock");
		documentService.unlock((DocumentInstance) event.getInstance());
	}

	/**
	 * Builds a document properties list fields for preview.
	 */
	public void renderDocumentFieldsList() {
		log.debug("Executed DocumentAction.renderDocumentFieldsList");

		UIComponent documentDataPanel = RichFunction
				.findComponent(CMFConstants.DOCUMENT_DATA_PANEL);

		if (documentDataPanel != null) {
			documentDataPanel.getChildren().clear();
		}

		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();

		DocumentDefinitionRef documentDefinition = (DocumentDefinitionRef) dictionaryService
				.getInstanceDefinition(documentInstance);

		invokeReader(documentDefinition, documentInstance, documentDataPanel, FormViewMode.PREVIEW,
				null);
	}

	/**
	 * Return the full path to a document. Currently it is whether image or pdf
	 * 
	 * @param docInstance
	 *            is the document to get preview path for
	 * @return pdf file path
	 */
	public String getDocumentPreviewURL(DocumentInstance docInstance) {
		documentPreviewPath = "";
		previewSourceType = null;
		FileDescriptor documentPreview = documentService.getDocumentPreview(docInstance);

		if (documentPreview instanceof ContentPreviewDescriptor) {
			ExternalContext externalContext = FacesContext.getCurrentInstance()
					.getExternalContext();
			String requestServerName = externalContext.getRequestServerName();
			int requestServerPort = externalContext.getRequestServerPort();
			documentPreviewPath = new StringBuilder(externalContext.getRequestScheme())
					.append("://").append(requestServerName).append(":").append(requestServerPort)
					.append(externalContext.getRequestContextPath())
					.append(documentPreview.getId()).append(IMAGE_URL_VERSION_SUFFIX)
					.append(docInstance.getProperties().get(DocumentProperties.VERSION)).toString();
			previewSourceType = ((ContentPreviewDescriptor) documentPreview).getMimetype()
					.startsWith("image/") ? "image" : "pdf";

		}

		log.debug("document preview url [" + documentPreviewPath + "]");

		return documentPreviewPath;
	}

	/**
	 * Gets the document preview path.
	 * 
	 * @return the document preview path
	 */
	public String getDocumentPreviewPath() {
		log.debug("Executing DocumentAction.getDocumentPreviewPath");
		return documentPreviewPath;
	}

	/**
	 * Getter method for shareLink.
	 * 
	 * @return the shareLink
	 */
	public String getShareLink() {
		log.debug("Executing DocumentAction.getShareLink");
		return documentPreviewPath;
	}

	/**
	 * Checks if is allowed operation.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param operation
	 *            the operation
	 * @return true, if is allowed operation
	 */
	public boolean isAllowedOperation(DocumentInstance documentInstance, String operation) {

		boolean isAllowed = false;

		Pair<Role, RoleEvaluator<DocumentInstance>> userRoleAndEvalutor = documentRoleEvaluator
				.evaluate(documentInstance, currentUser, null);

		Role userRole = userRoleAndEvalutor.getFirst();
		if (userRole != null) {
			Map<Permission, List<Pair<Class<?>, Action>>> permissions = userRole.getPermissions();
			Serializable locked = documentInstance.getProperties()
					.get(DocumentProperties.LOCKED_BY);

			// indicator for closed case
			Instance context = InstanceUtil.getContext(documentInstance, true);
			Serializable caseClosedReason = null;
			if (context instanceof CaseInstance) {
				caseClosedReason = context.getProperties().get(CaseProperties.CLOSED_REASON);
			}

			Permission permissionRevert = new EmfPermission("revert");

			if (ActionTypeConstants.HISTORY_PREVIEW.equals(operation)
					&& permissions.containsKey(SecurityModel.PERMISSION_READ)
					&& !documentHistoryVersion && (locked == null)) {
				isAllowed = true;
			} else if (ActionTypeConstants.DOWNLOAD.equals(operation)
					&& permissions.containsKey(SecurityModel.PERMISSION_READ)) {
				isAllowed = true;
			} else if (ActionTypeConstants.REVERT.equals(operation)
					&& permissions.containsKey(permissionRevert) && (locked == null)
					&& (caseClosedReason == null) && !documentHistoryVersion) {
				isAllowed = true;
			}
		}

		return isAllowed;
	}

	/**
	 * Gets the historical doc version.
	 * 
	 * @param docInstance
	 *            the doc instance
	 * @param version
	 *            the version
	 * @return the historical doc version
	 */
	public String getHistoricalDocVersion(DocumentInstance docInstance, String version) {
		// save reference to latest version
		if (getDocumentContext().get(DocumentConstants.DOCUMENT_LATEST_VERSION) == null) {
			getDocumentContext().put(DocumentConstants.DOCUMENT_LATEST_VERSION, docInstance);
		}

		DocumentInstance historyVersion = documentService.getDocumentVersion(docInstance, version);
		setDocumentHistoryVersion(true);
		return caseDocumentsTableAction.open(historyVersion);
	}

	/**
	 * Download historical document version.
	 * 
	 * @param documentInstance
	 *            current document instance
	 * @param version
	 *            current document version
	 * @return path to the document
	 */
	public String downloadHistoricalDocVersion(DocumentInstance documentInstance, String version) {
		DocumentInstance historyVersion = documentService.getDocumentVersion(documentInstance,
				version);
		if (historyVersion == null) {
			return "";
		}
		return getDocumentDownloadUrl(historyVersion);
	}

	/**
	 * Gets the latest doc version.
	 * 
	 * @return the latest doc version
	 */
	public String getLatestDocVersion() {
		setDocumentHistoryVersion(false);
		String navigationString = NavigationConstants.RELOAD_PAGE;

		if (getDocumentContext().get(DocumentConstants.DOCUMENT_LATEST_VERSION) != null) {
			DocumentInstance latestInstance = (DocumentInstance) getDocumentContext().remove(
					DocumentConstants.DOCUMENT_LATEST_VERSION);
			navigationString = caseDocumentsTableAction.open(latestInstance);
		}

		return navigationString;
	}

	/**
	 * This method apply styles by given document instance. Based on the document type - outgoing or
	 * incoming will apply style prefixes for RNC support. If current document instance is not
	 * available will apply default class.
	 * 
	 * @param documentInstance
	 *            current document instance
	 * @param isOutgoingDocument
	 *            boolean parameter that check for outgoing document
	 * @return group of styles for document
	 */
	public String getStylesForDocumentType(DocumentInstance documentInstance,
			boolean isOutgoingDocument) {
		if (documentInstance != null) {

			// builder that will hold document styles
			StringBuilder styles = new StringBuilder(DocumentInstance.class.getSimpleName()
					.toLowerCase());
			String stylePrefix = BuilderCssConstants.CMF_INCOMING_DOCUMENT_PREFIX;

			// is current document of type outgoing
			if (isOutgoingDocument) {
				stylePrefix = BuilderCssConstants.CMF_OUTGOING_DOCUMENT_PREFIX;
			}
			styles.append(" ").append(stylePrefix);
			styles.append(documentInstance.getIdentifier());
			return styles.toString();
		}
		// document instance is not available, pass
		// the default style
		return BuilderCssConstants.CMF_DOCUMENT_DEFAILT_CSS_CLASS;
	}

	/**
	 * Checks if the document is image.
	 * 
	 * @return true if the document is image
	 */
	public boolean isDocumentImage() {
		if (getDocumentContext().getInstance(DocumentInstance.class) != null) {
			String mimetype = (String) getDocumentContext().getInstance(DocumentInstance.class)
					.getProperties().get(DocumentProperties.MIMETYPE);
			if (StringUtils.isNotNullOrEmpty(mimetype)) {
				return mimetype.startsWith("image");
			}
		}
		return false;
	}

	/**
	 * Checks if the document is X3D.
	 * 
	 * @return true if the document is X3D
	 */
	public boolean isDocumentScene() {
		if (getDocumentContext().getInstance(DocumentInstance.class) != null) {
			String mimetype = (String) getDocumentContext().getInstance(DocumentInstance.class)
					.getProperties().get(DocumentProperties.MIMETYPE);
			if (StringUtils.isNotNullOrEmpty(mimetype)) {
				// is this check reliable
				return mimetype.startsWith("application/octet-stream");
			}
		}
		return false;
	}

	/**
	 * Get source type. For images is image, for other documents is pdf, for not available preview
	 * is null
	 * 
	 * @return the preview type
	 */
	public String getPreviewSourceType() {

		return previewSourceType;
	}

	/**
	 * Setter method for shareLink.
	 * 
	 * @param shareLink
	 *            the shareLink to set
	 */
	public void setShareLink(String shareLink) {
		//
	}

	/**
	 * Getter method for iframeSource.
	 * 
	 * @return the iframeSource
	 */
	public String getIframeSource() {
		return iframeSource;
	}

	/**
	 * Setter method for iframeSource.
	 * 
	 * @param iframeSource
	 *            the iframeSource to set
	 */
	public void setIframeSource(String iframeSource) {
		this.iframeSource = iframeSource;
	}

	/**
	 * Getter method for sectionInstanceId.
	 * 
	 * @return the sectionInstanceId
	 */
	public String getSectionInstanceId() {
		return sectionInstanceId;
	}

	/**
	 * Setter method for sectionInstanceId.
	 * 
	 * @param sectionInstanceId
	 *            the sectionInstanceId to set
	 */
	public void setSectionInstanceId(String sectionInstanceId) {
		this.sectionInstanceId = sectionInstanceId;
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
	 * Getter method for documentHistoryVersion.
	 * 
	 * @return the documentHistoryVersion
	 */
	public boolean isDocumentHistoryVersion() {
		return documentHistoryVersion;
	}

	/**
	 * Setter method for documentHistoryVersion.
	 * 
	 * @param documentHistoryVersion
	 *            the documentHistoryVersion to set
	 */
	public void setDocumentHistoryVersion(boolean documentHistoryVersion) {
		this.documentHistoryVersion = documentHistoryVersion;
	}

	/**
	 * Setter method for documentPreviewPath.
	 * 
	 * @param documentPreviewPath
	 *            the documentPreviewPath to set
	 */
	public void setDocumentPreviewPath(String documentPreviewPath) {
		this.documentPreviewPath = documentPreviewPath;
	}

}
