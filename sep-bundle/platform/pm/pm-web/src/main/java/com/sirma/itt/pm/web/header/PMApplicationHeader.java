package com.sirma.itt.pm.web.header;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.header.EmfApplicationHeader;
import com.sirma.itt.emf.web.plugin.PageFragment;

/**
 * The Class PMApplicationHeader.
 * 
 * @author svelikov
 */
public class PMApplicationHeader extends EmfApplicationHeader {

	/**
	 * The Class ProjectTitle.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 10, priority = 2)
	public static class ProjectTitle implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/menu/header/project-title-header.xhtml";
		}
	}

}
