package com.sirma.itt.emf.cls.label;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for resources located in cls-web module.
 * 
 * @author Mihail Radkov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 66)
public class ClsWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.cls.i18n.i18n";
	}

}
