package com.sirma.itt.seip.instance.labels;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in the instance-core module.
 *
 * @author nvelkov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 71)
public class InstanceCoreLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.seip.instance.i18n.i18n";
	}

}
