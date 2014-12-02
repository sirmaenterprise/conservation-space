package com.sirma.itt.emf.definition;

import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.adapter.FileDescriptor;

/**
 * Provides information for definitions and configuration such as type definition locations and the
 * means for retrieving them from remote or local sources. Provides also the EMF enabled containers. <br>
 * We have a single method for loading definitions with argument the type of the definitions to
 * load.
 * 
 * @author BBonev
 */
public interface DefinitionManagementService {

	/**
	 * Fetches the locations of the available definitions for loading.
	 * 
	 * @param definitionClass
	 *            the definition class
	 * @return the list of locations to load
	 */
	public List<FileDescriptor> getDefinitions(Class<?> definitionClass);

	/**
	 * Gets the enabled EMF containers.
	 * 
	 * @return the enabled EMF containers
	 */
	public Set<String> getEnabledEmfContainers();
}
