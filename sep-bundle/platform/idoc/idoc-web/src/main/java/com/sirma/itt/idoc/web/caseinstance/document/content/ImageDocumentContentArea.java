package com.sirma.itt.idoc.web.caseinstance.document.content;

import javax.inject.Inject;

import com.sirma.cmf.web.document.DocumentAction;
import com.sirma.cmf.web.document.content.DocumentContentAreaProvider;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * This class adds image viewer to image document.
 * 
 * @author SKostadinov
 */
@Extension(target = DocumentContentAreaProvider.EXTENSION_POINT, enabled = true, order = 1, priority = 1)
public class ImageDocumentContentArea implements DocumentContentAreaProvider {

	@Inject
	private DocumentAction documentAction;

	@Override
	public String getPath() {
		return "/document/includes/document-viewer.xhtml";
	}

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		return documentAction.isDocumentImage();
	}

	@Override
	public void handle(DocumentInstance documentInstance) {
		// Empty block.
	}
}