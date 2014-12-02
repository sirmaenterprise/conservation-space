package com.sirma.itt.pm.web.caseinstance;

import com.sirma.cmf.web.caseinstance.CaseListToolbarExtensionPoint;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * Definitions of PM plugins for CaseListToolbarExtensionPoint.
 * 
 * @author svelikov
 */
public class PmCaseListToolbarExtensionPoint extends CaseListToolbarExtensionPoint {

	/**
	 * The Class CreateCaseAction with higher priority to override the default plugin from CMF
	 * module.
	 */
	@Extension(target = EXTENSION_POINT, order = 10, enabled = true, priority = 2)
	public static class CreateCaseAction implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/includes/pm-create-case-button.xhtml";
		}

	}

}
