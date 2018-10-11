package com.sirma.itt.seip.rest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The MapperExtension is plugin to extend jackson mapper with customized behavior
 * 
 * @author bbanchev
 */
public interface MapperExtension extends Plugin {
	/** Plugin name */
	String NAME = "MapperExtensionPlugin";

	/**
	 * Extend a given mapper provided as argument with custom modules.
	 *
	 * @param mapper
	 *            the mapper to extend
	 */
	void extend(ObjectMapper mapper);

}
