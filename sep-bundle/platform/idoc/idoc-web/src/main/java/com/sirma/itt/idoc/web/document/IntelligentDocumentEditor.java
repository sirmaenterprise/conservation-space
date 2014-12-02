package com.sirma.itt.idoc.web.document;

import com.sirma.cmf.web.document.editor.DocumentEditor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * @author Adrian Mitev
 */
@Extension(target = DocumentEditor.EXTENSION_POINT, order = -1)
public class IntelligentDocumentEditor implements DocumentEditor {

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		return IntelligentDocumentProperties.DOCUMENT_PURPOSE.equalsIgnoreCase(documentInstance
				.getPurpose());
	}

	@Override
	public void handle(DocumentInstance documentInstance, boolean preview) {

	}

	@Override
	public String getPath() {
		return "/document-details.xhtml";
	}

}
