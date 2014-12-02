package com.sirma.itt.emf.dozer.provider;

import java.util.List;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * Provider interface and extension point for adding additional mapping files.
 *
 * @author BBonev
 */
public interface DozerMapperMappingProvider extends Plugin {

	/** The target name. */
	String TARGET_NAME = "dozerMappingProvider";

	/**
	 * Gets the mapping URI into the context of the current class loader
	 *
	 * @return the mapping URI
	 */
	List<String> getMappingUries();
}