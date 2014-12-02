package com.sirma.itt.emf.web.resources.sprites;

import com.sirma.itt.emf.plugin.Extension;

/**
 * The Class CmfSpriteDefinition.
 * 
 * @author svelikov
 */
@Extension(target = SpriteDefinition.DIRECTORY_TARGET_NAME, order = 0.1)
public class EmfSpriteDefinition implements SpriteDefinition {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return "/images";
	}

}
