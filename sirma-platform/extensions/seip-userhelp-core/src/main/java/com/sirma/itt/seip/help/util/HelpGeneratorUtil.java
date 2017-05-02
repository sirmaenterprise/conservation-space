package com.sirma.itt.seip.help.util;

import java.io.File;
import java.io.FilenameFilter;

import com.sirma.itt.seip.help.PropertyConfigsWrapper;

/**
 * Help constants.
 */
public class HelpGeneratorUtil {
	private HelpGeneratorUtil() {
	}

	/** The Constant NL. */
	static final String NL = "\n";

	static final String CONFIG_CUSTOM_FOOTER = PropertyConfigsWrapper.INSTANCE.getProperty("help.footer.custom");

	static final String CONFIG_EXTERNAL_URL = PropertyConfigsWrapper.INSTANCE.getProperty("project.url.confluence");

	static final String FILE_EXTENSION_HTML = ".html";
	/** UTF encoding. */
	public static final String UTF_8 = "UTF-8";

	static File[] listHtmlFiles(File htmlLocation) {
		File[] htmlFiles = htmlLocation.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(FILE_EXTENSION_HTML);
			}
		});
		if (htmlFiles == null) {
			return new File[0];
		}
		return htmlFiles;
	}

	static String customFooter() {
		return CONFIG_CUSTOM_FOOTER;
	}
}
