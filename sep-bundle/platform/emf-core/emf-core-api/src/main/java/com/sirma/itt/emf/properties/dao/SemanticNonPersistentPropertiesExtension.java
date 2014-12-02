package com.sirma.itt.emf.properties.dao;

import java.util.Set;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension point to define the non persistent properties for semantic database. If property is
 * returned from an extension it will not be persisted
 * 
 * @author BBonev
 */
@Documentation("Extension point to define the non persistent properties for semantic database. If property is returned from an extension it will not be persisted")
public interface SemanticNonPersistentPropertiesExtension extends Plugin {
	/** The target name. */
	String TARGET_NAME = "semanticNonPersistentProperties";

	/**
	 * Gets the non persistent properties.
	 * 
	 * @return the non persistent properties
	 */
	Set<String> getNonPersistentProperties();
}
