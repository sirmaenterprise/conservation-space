package com.sirma.itt.objects.web.actionsmanager;

import com.sirma.cmf.web.actionsmanager.ActionButtonTemplateExtensionPoint;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extensions for action buttons template.
 *
 * @author svelikov
 */
public class ObjectsActionButtonTemplateExtensions implements ActionButtonTemplateExtensionPoint {

	/**
	 * The Class ObjectMoveSameCase.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 1000, priority = 1)
	public static class ObjectMoveSameCase implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/common/object-action-button-template.xhtml";
		}
	}

}
