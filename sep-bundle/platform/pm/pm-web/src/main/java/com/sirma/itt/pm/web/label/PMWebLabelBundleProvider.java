package com.sirma.itt.pm.web.label;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for resources located in pm-web module.
 * 
 * @author Adrian Mitev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 2)
public class PMWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.pm.i18n.i18n";
	}

}
