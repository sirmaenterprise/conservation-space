package com.sirma.itt.idoc.web.util;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * @author yasko
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 25)
public class IdocLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.idoc.i18n.i18n";
	}

}
