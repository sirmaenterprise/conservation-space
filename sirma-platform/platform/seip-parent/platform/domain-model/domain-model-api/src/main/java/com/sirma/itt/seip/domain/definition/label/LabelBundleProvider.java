package com.sirma.itt.seip.domain.definition.label;

import java.util.Locale;
import java.util.ResourceBundle;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides an information about the names of the deployed bundle name. The bundle name should be defined in the
 * faces-config.xml
 *
 * @author BBonev
 */
public interface LabelBundleProvider extends Plugin {

	/** The plugin name. */
	String TARGET_NAME = "bundleProvider";

	/**
	 * Gets the bundle.
	 *
	 * @param locale
	 *            language for which a bundle should be fetched.
	 * @return the bundle
	 */
	ResourceBundle getBundle(Locale locale);
}
