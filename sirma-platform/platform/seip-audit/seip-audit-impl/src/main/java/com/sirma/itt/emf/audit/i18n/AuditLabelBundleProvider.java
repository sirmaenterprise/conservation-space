package com.sirma.itt.emf.audit.i18n;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for audit module.
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 68.5)
public class AuditLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.emf.audit.i18n.labels";
	}

}
