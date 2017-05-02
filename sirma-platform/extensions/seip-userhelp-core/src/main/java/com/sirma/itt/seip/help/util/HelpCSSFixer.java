package com.sirma.itt.seip.help.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.sirma.itt.seip.help.exception.HelpGenerationException;

/**
 * The HelpCSSFixer updates the existing css with custom rules.
 */
public class HelpCSSFixer {
	private HelpCSSFixer() {
	}

	/**
	 * Fix css style - update existing and add new.
	 *
	 * @param helpFilesFolder
	 *            the htmls location
	 * @param customCss
	 *            is list of custom css rules
	 */
	public static void fix(File helpFilesFolder, String... customCss) {
		try {

			File siteCss = new File(helpFilesFolder, "styles" + File.separatorChar + "site.css");
			List<String> css = FileUtils.readLines(siteCss);
			css
					.add("#footer .footer-body-sirma { background-image: url(\"../../images/Sirma_logo.png\"); 	background-repeat: no-repeat; 	background-position-x: center; 	background-position-y: bottom; background-position-x: center; min-height:62px; margin:0;}");
			css.add("");
			css.add("* {");
			css.add("	max-width: 10in;");
			css.add("}");
			css.add("");
			css.add("@media print {");
			css.add("  .bigImage { width: 680px; }");
			css.add("}");

			if (customCss != null) {
				css.addAll(Arrays.asList(customCss));
			}

			FileUtils.writeLines(siteCss, css);
		} catch (Exception e) {
			throw new HelpGenerationException(e);
		}
	}
}
