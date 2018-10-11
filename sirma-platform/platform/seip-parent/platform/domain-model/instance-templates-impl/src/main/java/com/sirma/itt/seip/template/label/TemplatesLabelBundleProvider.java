package com.sirma.itt.seip.template.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in instance-templates-impl module.
 *
 * @author nvelkov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 69)
public class TemplatesLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.seip.template.i18n.i18n";
	}

}
