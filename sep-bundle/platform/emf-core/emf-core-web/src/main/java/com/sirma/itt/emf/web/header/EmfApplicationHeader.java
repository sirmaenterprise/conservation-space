package com.sirma.itt.emf.web.header;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * UI extension point for application header region below navigation menu. This header is used to
 * render application title if just CMF module is deployed or any other content that may be provided
 * as extension.
 * 
 * @author svelikov
 */
@Named
public class EmfApplicationHeader implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "applicationHeader";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * The Class TitleHeader.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 1)
	public static class TitleHeader implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/header/application-title.xhtml";
		}
	}

}
