/**
 * Copyright (c) 2013 16.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.cmf.web.document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for the content of the list with sections and documents defined in
 * /case/case-document.xhtml.
 * 
 * @author Adrian Mitev
 */
@Named
@ApplicationScoped
public class DocumentsListExtensionPoint implements Plugable {

	public static final String EXTENSION_POINT = "case:documentList:content";

	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * Extension for the {@link DocumentsListExtensionPoint} adding modal panel for filling initial
	 * data when creating a new document.
	 * 
	 * @author Adrian Mitev
	 */
	@Extension(target = EXTENSION_POINT, order = 1, priority = 0)
	public static class DocumentCreateDialogExtension implements PageFragment {

		@Override
		public String getPath() {
			return "/common/create-document.xhtml";
		}

	}

}
