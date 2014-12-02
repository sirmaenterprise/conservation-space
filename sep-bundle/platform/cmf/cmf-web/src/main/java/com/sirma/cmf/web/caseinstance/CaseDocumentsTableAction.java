package com.sirma.cmf.web.caseinstance;

import java.io.Serializable;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.TableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.document.content.DocumentContentAreaProvider;
import com.sirma.cmf.web.document.editor.DocumentEditor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.event.document.DocumentOpenEvent;
import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * Backing bean for documents list table.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseDocumentsTableAction extends TableAction implements Serializable {

	private static final long serialVersionUID = 6042499806553671486L;

	@Inject
	private Event<DocumentOpenEvent> documentOpenEvent;

	/** All registered document editor plugins. */
	@Inject
	@ExtensionPoint(DocumentEditor.EXTENSION_POINT)
	private Iterable<DocumentEditor> documentEditors;

	/** All registered document content area providers. */
	@Inject
	@ExtensionPoint(DocumentContentAreaProvider.EXTENSION_POINT)
	private Iterable<DocumentContentAreaProvider> documentContentAreaProviders;

	/**
	 * Opens the selected document instance for preview.
	 * 
	 * @param selectedDocumentInstance
	 *            Selected {@link DocumentInstance}.
	 * @return Navigation string.
	 */
	public String open(final DocumentInstance selectedDocumentInstance) {
		return open(selectedDocumentInstance, true);
	}

	/**
	 * Opens the selected document instance for edit.
	 * 
	 * @param selectedDocumentInstance
	 *            Selected {@link DocumentInstance}.
	 * @return Navigation string.
	 */
	public String openForEdit(final DocumentInstance selectedDocumentInstance) {
		return open(selectedDocumentInstance, false);
	}

	/**
	 * Opens the selected document instance.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @param preview
	 *            the preview
	 * @return the string
	 */
	private String open(final DocumentInstance selectedDocumentInstance, boolean preview) {
		log.debug("CMFWeb: Execute CaseDocumentsTableAction.open - document: "
				+ selectedDocumentInstance.getProperties().get(DocumentProperties.NAME));

		// fire an event that a document is to be opened
		documentOpenEvent.fire(new DocumentOpenEvent(selectedDocumentInstance));

		// default navigation is to reload the page
		String navigationString = NavigationConstants.RELOAD_PAGE;

		// try to find a suitable editor for the current document
		DocumentEditor foundDocumentEditor = findDocumentEditor(selectedDocumentInstance);
		// if a suitable editor is found, let it handle the document
		navigationString = invokeEditorHandler(selectedDocumentInstance, preview, navigationString,
				foundDocumentEditor);

		DocumentContentAreaProvider contentAreaProvider = findDocumentContentAreaProvider(selectedDocumentInstance);
		invokeContentAreaProviderHandler(selectedDocumentInstance, contentAreaProvider);

		return navigationString;
	}

	/**
	 * Invoke content area provider handler.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param contentAreaProvider
	 *            the content area provider
	 */
	private void invokeContentAreaProviderHandler(DocumentInstance documentInstance,
			DocumentContentAreaProvider contentAreaProvider) {
		if (contentAreaProvider != null) {
			contentAreaProvider.handle(documentInstance);
		}
	}

	/**
	 * Invoke editor handler.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @param preview
	 *            the preview
	 * @param navigationString
	 *            the navigation string
	 * @param foundDocumentEditor
	 *            the found document editor
	 * @return the string
	 */
	protected String invokeEditorHandler(final DocumentInstance selectedDocumentInstance,
			boolean preview, String navigationString, DocumentEditor foundDocumentEditor) {
		String navigation = navigationString;
		if (foundDocumentEditor != null) {
			foundDocumentEditor.handle(selectedDocumentInstance, preview);
			navigation = NavigationConstants.NAVIGATE_DOCUMENT_DETAILS;
			getDocumentContext().setDocumentInstance(selectedDocumentInstance);
		}
		return navigation;
	}

	/**
	 * Find document content area provider. We have different content viewer-a. Scene, Image and
	 * default that opens the document in a pdf viewer.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @return the document content area provider
	 */
	private DocumentContentAreaProvider findDocumentContentAreaProvider(
			DocumentInstance selectedDocumentInstance) {
		DocumentContentAreaProvider found = null;
		for (DocumentContentAreaProvider current : documentContentAreaProviders) {
			if (current.canHandle(selectedDocumentInstance)) {
				found = current;
				break;
			}
		}
		return found;
	}

	/**
	 * Find document editor. The document editor that would be responsible for editing of current
	 * document instance.
	 * 
	 * @param selectedDocumentInstance
	 *            the selected document instance
	 * @return the document editor
	 */
	protected DocumentEditor findDocumentEditor(final DocumentInstance selectedDocumentInstance) {
		DocumentEditor foundEditor = null;
		for (DocumentEditor current : documentEditors) {
			if (current.canHandle(selectedDocumentInstance)) {
				foundEditor = current;
				break;
			}
		}
		return foundEditor;
	}

	/**
	 * Getter method for documentEditor.
	 * 
	 * @param instance
	 *            the instance
	 * @return the documentEditor
	 */
	public DocumentEditor getDocumentEditor(DocumentInstance instance) {
		return findDocumentEditor(instance);
	}

	/**
	 * Getter method for documentContentAreaProvider.
	 * 
	 * @param instance
	 *            the instance
	 * @return the documentContentAreaProvider
	 */
	public DocumentContentAreaProvider getDocumentContentAreaProvider(DocumentInstance instance) {
		return findDocumentContentAreaProvider(instance);
	}

}
