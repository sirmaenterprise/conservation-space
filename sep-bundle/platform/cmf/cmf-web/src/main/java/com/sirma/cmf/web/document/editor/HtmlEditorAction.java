package com.sirma.cmf.web.document.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Handles the edit and preview of HTML documents in the document details page.
 * 
 * @author Adrian Mitev
 */
@Named
@ViewAccessScoped
public class HtmlEditorAction extends Action implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8514315984432047349L;

	/** The case instance service. */
	@Inject
	private CaseService caseInstanceService;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/**
	 * Sets the edited content to the currently edited DocumentInstance and saves the entire
	 * CaseInstance.
	 */
	public void save() {
		// transform the edited content to byte array and store it as file locator
		byte[] contentAsArray = null;
		try {
			String content = (String) getDocumentContext().get("documentContent");
			contentAsArray = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}

		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();

		Map<String, Serializable> properties = documentInstance.getProperties();
		String documentName = (String) properties.get(DocumentProperties.NAME);
		properties.put(DocumentProperties.FILE_LOCATOR, new ByteArrayFileDescriptor(documentName,
				contentAsArray));
		if (contentAsArray != null) {
			properties.put(DocumentProperties.FILE_SIZE, Integer.valueOf(contentAsArray.length));
		}

		// unlock document
		// the whole CaseInstance should be saved after document modification
		// NOTE: case instance will be saved after unlocking the document
		if (SequenceEntityGenerator.isPersisted(documentInstance) && documentInstance.isLocked()) {
			documentService.unlock(documentInstance);
		} else {
			Operation operation = new Operation(ActionTypeConstants.EDIT_DETAILS);
			documentService.save(documentInstance, operation);
			RuntimeConfiguration.setConfiguration(
					RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
			try {
				CaseInstance parent = InstanceUtil.getParent(CaseInstance.class, documentInstance);
				caseInstanceService.save(parent, operation);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}
		// the document goes in preview mode after saving
		getDocumentContext().put("documentPreviewMode", Boolean.TRUE);
	}

	/**
	 * Cancels document editing. If the document was not yet saved (id=null), delete it from the
	 * case. If the document was already saved, just reload the content from the document and
	 * redisplay the page.
	 * 
	 * @return navigation string.
	 */
	public String cancel() {
		DocumentInstance documentInstance = getDocumentContext().getDocumentInstance();
		// if document was locked then we need to unlock it
		if (SequenceEntityGenerator.isPersisted(documentInstance) && documentInstance.isLocked()) {
			documentService.unlock(documentInstance);
		}
		handle(documentInstance, true);

		String navigation = NavigationConstants.RELOAD_PAGE;
		if (!SequenceEntityGenerator.isPersisted(documentInstance)) {
			navigation = NavigationConstants.NAVIGATE_TAB_CASE_DOCUMENTS;
		}
		return navigation;
	}

	/**
	 * Handle.
	 * 
	 * @param documentInstance
	 *            the document instance
	 * @param preview
	 *            the preview
	 */
	private void handle(DocumentInstance documentInstance, boolean preview) {
		getDocumentContext().put("documentPreviewMode", Boolean.TRUE);
		getDocumentContext().setDocumentInstance(documentInstance);

		if (!preview && SequenceEntityGenerator.isPersisted(documentInstance)) {
			documentService.lock(documentInstance);
		}

		// if the document has content, load it in a String and store it in the content field
		String content = null;
		InputStream contentStream = documentService.getContentStream(documentInstance);
		if (contentStream != null) {
			try {
				content = IOUtils.toString(contentStream, "UTF-8");
			} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				try {
					contentStream.close();
				} catch (IOException e) {
					log.trace("", e);
				}
			}
		}
		getDocumentContext().put("documentContent", content);
	}

}
