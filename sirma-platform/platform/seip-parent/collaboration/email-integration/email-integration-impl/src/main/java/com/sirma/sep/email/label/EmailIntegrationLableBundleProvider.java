package com.sirma.sep.email.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;


/**
 * Provides labels for resources located in EmailIntegration module.
 *
 * @author svelikov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 95)
public class EmailIntegrationLableBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.sep.email.i18n.i18n";
	}

}