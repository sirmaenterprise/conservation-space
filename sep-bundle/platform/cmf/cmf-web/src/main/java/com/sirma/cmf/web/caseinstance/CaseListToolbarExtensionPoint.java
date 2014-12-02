package com.sirma.cmf.web.caseinstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for case list toolbar.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class CaseListToolbarExtensionPoint implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "case.list.toolbar.extension.point";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * Extension that provides a create case button for the case list toolbar.
	 */
	@Extension(target = EXTENSION_POINT, order = 10, enabled = true, priority = 1)
	public static class CreateCaseAction implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/create-case-button.xhtml";
		}

	}

}
