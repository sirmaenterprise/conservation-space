package com.sirma.itt.emf.properties.dao;

import java.util.Set;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension point for {@link PropertiesDao} to define the non persistent properties. If property is
 * returned from an extension it will not be persisted not even using the
 * {@link com.sirma.itt.emf.configuration.RuntimeConfigurationProperties#SAVE_PROPERTIES_WITHOUT_DEFINITION}
 * .<br>
 * 
 * @author BBonev
 */
@Documentation("Extension point for {@link PropertiesDao} to define the non persistent properties. If property is returned from an extension it will not be persisted not even using the {@link RuntimeConfigurationProperties#SAVE_PROPERTIES_WITHOUT_DEFINITION}.")
public interface RelationalNonPersistentPropertiesExtension extends Plugin {
	/** The target name. */
	String TARGET_NAME = "relationalNonPersistentProperties";

	/**
	 * Gets the non persistent properties.
	 * 
	 * @return the non persistent properties
	 */
	Set<String> getNonPersistentProperties();
}
