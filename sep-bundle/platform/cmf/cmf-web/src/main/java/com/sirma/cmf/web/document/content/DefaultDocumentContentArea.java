package com.sirma.cmf.web.document.content;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * DefaultDocumentContentArea provider extension.
 * 
 * @author svelikov
 */
@Extension(target = DocumentContentAreaProvider.EXTENSION_POINT, order = 10, enabled = true, priority = 1)
public class DefaultDocumentContentArea implements DocumentContentAreaProvider {

	@Override
	public String getPath() {
		return "/document/includes/default-document-content-area.xhtml";
	}

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		return true;
	}

	@Override
	public void handle(DocumentInstance documentInstance) {
		// not used
	}

}
