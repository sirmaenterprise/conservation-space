package com.sirma.itt.emf.definition;

import java.util.List;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.plugin.SupportablePlugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * Plugin for {@link DefinitionManagementService} that provides additional definition loading. If 2
 * or more extensions provide the same supported class type, then the final result will be the sum
 * of all provided definitions.
 * 
 * @author BBonev
 */
@Documentation("Plugin for {@link DefinitionManagementService} that provides additional definition loading. If 2 or more extensions provide the same supported class type, then the final result will be the sum of all provided definitions.")
public interface DefinitionManagementServiceExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "definitionManagementServiceExtension";

	/**
	 * Fetches the locations of the available definitions for loading.
	 * 
	 * @param definitionClass
	 *            the definition class
	 * @return the list of locations to load
	 */
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass);
}
