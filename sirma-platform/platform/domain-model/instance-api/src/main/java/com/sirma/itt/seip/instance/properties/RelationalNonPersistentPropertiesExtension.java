package com.sirma.itt.seip.instance.properties;

import java.util.Set;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point for {@link PropertiesDao} to define the non persistent properties. If property is returned from an
 * extension it will not be persisted not even using the {@link Options#SAVE_PROPERTIES_WITHOUT_DEFINITION} .<br>
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
