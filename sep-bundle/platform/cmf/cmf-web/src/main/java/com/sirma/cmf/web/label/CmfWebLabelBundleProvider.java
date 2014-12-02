package com.sirma.cmf.web.label;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for resources located in cmf-web module.
 * 
 * @author Adrian Mitev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 1)
public class CmfWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.cmf.i18n.i18n";
	}

}
