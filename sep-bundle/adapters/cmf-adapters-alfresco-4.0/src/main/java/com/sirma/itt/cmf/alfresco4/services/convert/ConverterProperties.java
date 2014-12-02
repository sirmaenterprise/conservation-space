package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.Properties;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * The Interface ConvertorProperties is extension for converter properties providers.
 */
@Documentation("The Interface ConverterProperties is extension for converter properties providers.")
public interface ConverterProperties extends Plugin {

	/** The target name. */
	String TARGET_NAME = "ConverterProperties";

	/**
	 * Gets the properties from this provider.
	 *
	 * @return the properties
	 */
	Properties getInternalProperties() throws Exception;

	/**
	 * Gets the external configured properties from this provider.
	 *
	 * @return the properties if there are
	 */
	Properties getExternalProperties() throws Exception;
}
