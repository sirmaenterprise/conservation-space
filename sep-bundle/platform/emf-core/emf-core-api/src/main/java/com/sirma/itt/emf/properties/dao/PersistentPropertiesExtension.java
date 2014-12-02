package com.sirma.itt.emf.properties.dao;

import java.util.Set;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension point for {@link PropertiesDao} to define the persistent properties that need to be
 * persisted without definition check.If property is returned from an extension it will be persisted
 * even not defined in any definition.<br>
 * REVIEW: probably is good idea the returned data from the provider method to be a mapping with key
 * the target instance that these properties belong
 * 
 * @author BBonev
 */
@Documentation("Extension point for {@link PropertiesDao} to define the persistent properties that need to be"
		+ " persisted without definition check.If property is returned from an extension it will be persisted"
		+ " even not defined in any definition.")
public interface PersistentPropertiesExtension extends Plugin {
	/** The target name. */
	String TARGET_NAME = "persistentProperties";

	/**
	 * Gets the non persistent properties.
	 * 
	 * @return the non persistent properties
	 */
	Set<String> getPersistentProperties();
}
