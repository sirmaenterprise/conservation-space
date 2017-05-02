package com.sirma.itt.emf.info;

import java.util.Properties;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The Interface VersionInfo is extension interface for version info providers.
 *
 * @author bbanchev
 */
@Documentation(value = "Version info for each module currently loaded")
public interface VersionInfo extends Plugin {

	/** The target name. */
	String TARGET_NAME = "ModulesVersionInfo";

	/**
	 * Gets the file info location. Currently is used the pom.properties file
	 *
	 * @return the file info location
	 */
	String getFileInfoLocation();

	/**
	 * Gets the file info as properties.
	 *
	 * @return the file info
	 */
	Properties getFileInfo();

	/**
	 * Gets the module description to be visualized
	 *
	 * @return the module description
	 */
	String getModuleDescription();
}
