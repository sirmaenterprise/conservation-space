package com.sirma.itt.idoc.widgets;

import com.sirma.itt.emf.label.ClasspathLabelBundleProvider;
import com.sirma.itt.emf.label.LabelBundleProvider;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Provides labels for the widgets.
 * 
 * @author Adrian Mitev
 */
@Extension(target = LabelBundleProvider.TARGET_NAME, order = 26)
public class WidgetsLabelBundleProvider extends ClasspathLabelBundleProvider {

	@Override
	protected String getBaseName() {
		return "com.sirma.itt.idoc.widgets.widgets";
	}

}
