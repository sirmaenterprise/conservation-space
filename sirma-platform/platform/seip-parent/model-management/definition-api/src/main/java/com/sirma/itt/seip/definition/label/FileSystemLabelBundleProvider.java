package com.sirma.itt.seip.definition.label;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.PathDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelBundleProvider;

/**
 * Provider for resource bundles located in the classpath. The implementors should provide only the basename of the
 * resource bundle.
 *
 * @author Adrian Mitev
 */
public abstract class FileSystemLabelBundleProvider implements LabelBundleProvider, PathDefinition {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public ResourceBundle getBundle(Locale locale) {
		try {
			String path = getPath();
			if (StringUtils.isNotBlank(path)) {
				File file = new File(getPath());
				if (file.exists()) {
					return fileToBundle(locale, file);
				}
				throw new IllegalArgumentException("File " + file.getAbsolutePath() + " cannot be found.");
			}

			return null;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static ResourceBundle fileToBundle(Locale locale, File file) throws MalformedURLException {
		try (URLClassLoader loader = new URLClassLoader(new URL[] { file.getParentFile().toURI().toURL() })) {
			return ResourceBundle.getBundle(removeSuffix(file.getName()), locale, loader);
		} catch (IOException e) {
			LOGGER.warn("", e);
			return null;
		}
	}

	/**
	 * Removes file suffix.
	 *
	 * @param name
	 *            file name.
	 * @return file name without the suffix..
	 */
	private static String removeSuffix(String name) {
		int suffixIndex = name.lastIndexOf('.');
		if (suffixIndex != -1) {
			return name.substring(0, suffixIndex);
		}
		return name;
	}

}
