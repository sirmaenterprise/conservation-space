package com.sirma.itt.emf.web.resources.sprites;

import com.sirma.itt.emf.plugin.PathDefinition;

/**
 * Definition for a sprite image or a directory of images.
 * 
 * @author Adrian Mitev
 */
public interface SpriteDefinition extends PathDefinition {

	String SINGLE_FILE_TARGET_NAME = "sprite.single";

	/**
	 * Path definition in this extension point should point to a directory within the application
	 * context or a directory inside a jar file located in *.jar/META-INF/resources. The path
	 * specified here should begin with /.
	 */
	String DIRECTORY_TARGET_NAME = "sprite.directory";

}
