package com.sirma.sep.labels;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in cmf-web module.
 *
 * @deprecated Used ATM, but will be removed soon.
 * @author Adrian Mitev
 */
@Deprecated
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 1)
public class CmfWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.cmf.i18n.i18n";
	}

}
