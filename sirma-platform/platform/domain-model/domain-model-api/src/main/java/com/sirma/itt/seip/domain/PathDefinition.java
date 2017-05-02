package com.sirma.itt.seip.domain;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Represents a plugin that defines a path to resource(s) that will be used.
 *
 * @author Adrian Mitev
 */
public interface PathDefinition extends Plugin {

	/**
	 * Provides path to the resource(s).
	 *
	 * @return resource path.
	 */
	String getPath();

}
