package com.sirma.cmf.web.folders;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;

/**
 * Manage operations before displaying folder content.
 *
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class FolderAction extends EntityAction implements Serializable {

	private static final long serialVersionUID = -8450684728985148765L;

	@Inject
	private UrlContextExtractor contextExtractor;

	/**
	 * Prepare context by given instance identifier and type.
	 */
	public void prepareContext() {
		contextExtractor.extractAndPopulateFromUrl();
	}

}
