package com.sirma.itt.emf.patch;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * All db schema patches should implement it and provide the class path for the xml file containing
 * the change log.
 * 
 * @author Adrian Mitev
 */
public interface DBSchemaPatch extends Plugin {

	String TARGET_NAME = "DBSchemaPatch";

	/**
	 * Provides the path to the patch inside the java classpath.
	 * 
	 * @return patch path.
	 */
	String getPath();

}
