package com.sirma.itt.idoc.web.document;

import com.sirma.cmf.web.document.DocumentsListExtensionPoint;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Used to replace the original create document dialog with create idoc dialog where the user cannot
 * input file name and title.
 * 
 * @author Adrian Mitev
 */
@Extension(target = DocumentsListExtensionPoint.EXTENSION_POINT, order = 1, priority = 1)
public class IntelligentDocumentCreateDialogExtension implements PageFragment {

	@Override
	public String getPath() {
		return "/common/create-idoc-dialog.xhtml";
	}

}
