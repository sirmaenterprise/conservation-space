package com.sirma.itt.seip.content.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in content-core module.
 *
 * @author Vilizar Tsonev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 67)
public class ContentCoreLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.seip.content.i18n.i18n";
	}
}
