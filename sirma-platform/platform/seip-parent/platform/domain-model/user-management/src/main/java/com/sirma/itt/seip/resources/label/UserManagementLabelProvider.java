package com.sirma.itt.seip.resources.label;

import com.sirma.itt.seip.definition.label.ClasspathLabelBundleProvider;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides labels for resources located in user-management module.
 *
 * @author smustafov
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 30)
public class UserManagementLabelProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.user.management.i18n.i18n";
	}

}
