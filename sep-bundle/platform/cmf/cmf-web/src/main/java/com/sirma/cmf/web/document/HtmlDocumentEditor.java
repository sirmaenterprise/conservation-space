package com.sirma.cmf.web.document;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.document.editor.DocumentEditor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The Class HtmlDocumentEditor is a plugin for documentEditor extension point. This can handle
 * documents with mimetype text/html and text/xhtml
 * 
 * @author svelikov
 */
@Named
@Extension(target = DocumentEditor.EXTENSION_POINT, order = 10, enabled = true, priority = 1)
public class HtmlDocumentEditor extends Action implements DocumentEditor {

	private static final String HTML_MIMETYPE = "text/html";

	private static final String XHTML_MIMETYPE = "text/xhtml";

	@Inject
	private DocumentService documentService;

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		String mimeType = (String) documentInstance.getProperties()
				.get(DocumentProperties.MIMETYPE);
		return StringUtils.isNotNull(mimeType)
				&& (HTML_MIMETYPE.equals(mimeType) || XHTML_MIMETYPE.equals(mimeType));
	}

	@Override
	public void handle(DocumentInstance documentInstance, boolean preview) {
		getDocumentContext().put("documentPreviewMode", preview);
		// maybe not needed
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
			}
		}
		getDocumentContext().put("documentContent", content);
	}

	@Override
	public String getPath() {
		return "/document/html-document-details-form.xhtml";
	}

}
