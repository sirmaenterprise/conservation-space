package com.sirma.itt.objects.web.caseinstance.tab;

import javax.enterprise.inject.Specializes;

import com.sirma.cmf.web.caseinstance.tab.CaseTabsExtensionPoint;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class ObjectsCaseTabsExtensionPoint.
 * 
 * @author svelikov
 */
@Specializes
public class ObjectsCaseTabsExtensionPoint extends CaseTabsExtensionPoint {

	/**
	 * The Class ObjectsTab.
	 */
	@Extension(target = EXTENSION_POINT, order = 35, enabled = true, priority = 1)
	public static class ObjectsTab implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/tab/objects.xhtml";
		}

	}

	/**
	 * The Class CaseRelationsTab.
	 */
	@Extension(target = EXTENSION_POINT, order = 50, enabled = true)
	public static class CaseRelationsTab implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/case/tab/relations.xhtml";
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
