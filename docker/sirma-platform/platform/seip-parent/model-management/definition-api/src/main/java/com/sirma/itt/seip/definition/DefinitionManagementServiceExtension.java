package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Plugin for {@link DefinitionManagementService} that provides additional definition loading. If 2 or more extensions
 * provide the same supported class type, then the final result will be the sum of all provided definitions.
 *
 * @author BBonev
 */
@Documentation("Plugin for {@link DefinitionManagementService} that provides additional definition loading. If 2 or more extensions provide the same supported class type, then the final result will be the sum of all provided definitions.")
public interface DefinitionManagementServiceExtension extends SupportablePlugin<Class> {

	/** The target name. */
	String TARGET_NAME = "definitionManagementServiceExtension";

	/**
	 * Fetches the locations of the available definitions for loading.
	 *
	 * @param definitionClass
	 *            the definition class
	 * @return the list of locations to load
	 */
	List<FileDescriptor> getDefinitions(Class<?> definitionClass);
}
