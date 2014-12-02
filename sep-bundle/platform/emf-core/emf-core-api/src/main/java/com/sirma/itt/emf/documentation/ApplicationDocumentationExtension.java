package com.sirma.itt.emf.documentation;

import java.util.Set;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension for application documentation generation API.
 * 
 * @author BBonev
 */
@Documentation("Extension for application documentation generation API.")
public interface ApplicationDocumentationExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "applicationDocumentationExtension";

	/**
	 * Generation options.
	 * 
	 * @return the sets the
	 */
	Set<String> generationOptions();

	/**
	 * Generate.
	 * 
	 * @param option
	 *            the option
	 * @return the string
	 */
	String generate(String option);
}
