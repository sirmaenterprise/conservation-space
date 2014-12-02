package com.sirma.itt.idoc.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.resources.JavascriptResourceExtensionPoint;

/**
 * This class manage the import of javascript files in the header of the page.
 * 
 * @author cdimitrov
 */
@Extension(target = JavascriptResourceExtensionPoint.EXTENSION_POINT, enabled = true, order = 60, priority = 1)
public class IdocJSResource implements JavascriptResourceExtensionPoint, PageFragment {

	@Override
	public String getPath() {
		return "/common/idoc-scripts.xhtml";
	}
}
