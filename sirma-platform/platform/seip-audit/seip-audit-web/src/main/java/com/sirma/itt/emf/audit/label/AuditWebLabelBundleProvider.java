package com.sirma.itt.emf.audit.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in BAM module.
 *
 * @author Mihail Radkov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 68)
public class AuditWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.emf.audit.i18n.i18n";
	}

}
