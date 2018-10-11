package com.sirma.itt.seip.mapping.dozer;

import java.util.List;

import com.sirma.itt.seip.plugin.Plugin;

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