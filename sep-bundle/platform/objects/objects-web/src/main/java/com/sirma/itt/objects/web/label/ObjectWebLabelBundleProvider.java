package com.sirma.itt.objects.web.label;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for resources located in object-web module.
 * 
 * @author svelikov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 3)
public class ObjectWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.object.i18n.i18n";
	}

}
