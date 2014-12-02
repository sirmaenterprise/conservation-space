package com.sirma.cmf.web.document;

import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.document.editor.DocumentEditor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * The Class StandardDocumentEditor is a plugin for documentEditor extension point. This is the
 * default document editor plugin and is a fall back editor if no one is found.
 * 
 * @author svelikov
 */
@Named
@Extension(target = DocumentEditor.EXTENSION_POINT, order = 1000, enabled = true, priority = 1)
public class StandardDocumentEditor extends Action implements DocumentEditor {

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		return true;
	}

	@Override
	public void handle(DocumentInstance documentInstance, boolean preview) {
		// not used
	}

	@Override
	public String getPath() {
		return "/document/document-details-form.xhtml";
	}

}
