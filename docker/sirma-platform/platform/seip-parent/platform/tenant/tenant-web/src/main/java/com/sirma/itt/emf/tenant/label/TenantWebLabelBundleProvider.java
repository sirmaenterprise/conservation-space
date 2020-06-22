package com.sirma.itt.emf.tenant.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelResolverProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in the tenant module.
 *
 * @author nvelkov
 */
@Extension(target = LabelResolverProvider.TARGET_NAME, order = 72)
public class TenantWebLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.sep.tenant.i18n.i18n";
	}

}
