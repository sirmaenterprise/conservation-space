package com.sirma.itt.idoc.web.caseinstance.document.content;

import javax.inject.Inject;

import com.sirma.cmf.web.document.DocumentAction;
import com.sirma.cmf.web.document.content.DocumentContentAreaProvider;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Extension;

/**
 * This class adds 3D viewer to X3D document.
 * 
 * @author SKostadinov
 */
@Extension(target = DocumentContentAreaProvider.EXTENSION_POINT, enabled = true, order = -1, priority = 10)
public class SceneDocumentContentArea implements DocumentContentAreaProvider {

	@Inject
	private DocumentAction documentAction;

	@Override
	public String getPath() {
		return "/document/includes/scene-viewer.xhtml";
	}

	@Override
	public boolean canHandle(DocumentInstance documentInstance) {
		return documentAction.isDocumentScene();
	}

	@Override
	public void handle(DocumentInstance documentInstance) {
		// Empty block.
	}
}