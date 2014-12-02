package com.sirma.itt.emf.label;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.plugin.PathDefinition;

/**
 * Provider for resource bundles located in the classpath. The implementors should provide only the
 * basename of the resource bundle.
 * 
 * @author Adrian Mitev
 */
public abstract class FileSystemLabelBundleProvider implements LabelBundleProvider, PathDefinition {

	@Override
	public ResourceBundle getBundle(Locale locale) {
		try {
			String path = getPath();
			if (StringUtils.isNotNullOrEmpty(path)) {
				File file = new File(getPath());
				if (file.exists()) {
					URL[] urls = { file.getParentFile().toURI().toURL() };
					ClassLoader loader = new URLClassLoader(urls);
					return ResourceBundle.getBundle(removeSuffix(file.getName()), locale, loader);
				}
				throw new IllegalArgumentException("File " + file.getAbsolutePath()
						+ " cannot be found.");
			}

			return null;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Removes file suffix.
	 * 
	 * @param name
	 *            file name.
	 * @return file name without the suffix..
	 */
	private String removeSuffix(String name) {
		int suffixIndex = name.lastIndexOf(".");
		if (suffixIndex != -1) {
			return name.substring(0, suffixIndex);
		}
		return name;
	}

}
