package com.sirma.itt.seip.instance.properties;

import java.util.Set;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point to define the non persistent properties for semantic database. If property is returned from an
 * extension it will not be persisted.
 *
 * @author BBonev
 */
@Documentation("Extension point to define the non persistent properties for semantic database. If property is returned from an extension it will not be persisted")
public interface SemanticNonPersistentPropertiesExtension extends Plugin {

	String TARGET_NAME = "semanticNonPersistentProperties";

	/**
	 * Gets the non persistent properties.
	 *
	 * @return the non persistent properties
	 */
	Set<String> getNonPersistentProperties();
}