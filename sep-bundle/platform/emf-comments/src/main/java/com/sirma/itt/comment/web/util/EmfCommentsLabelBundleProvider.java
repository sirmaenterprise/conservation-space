package com.sirma.itt.comment.web.util;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Label bundle provider for the comments module.
 * 
 * @author yasko
 * 
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 27)
public class EmfCommentsLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.comment.i18n.i18n";
	}

}