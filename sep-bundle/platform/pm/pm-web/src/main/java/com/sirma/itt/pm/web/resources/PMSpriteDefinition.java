package com.sirma.itt.pm.web.resources;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.resources.sprites.SpriteDefinition;

/**
 * The Class PMSpriteDefinition.
 * 
 * @author svelikov
 */
@Extension(target = SpriteDefinition.DIRECTORY_TARGET_NAME, order = 0.2)
public class PMSpriteDefinition implements SpriteDefinition {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return "/images";
	}

}
