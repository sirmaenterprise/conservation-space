package com.sirma.itt.cmf.extensions;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.cmf.constants.NonPersistentProperties;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for the property keys that should be removed from an instance when persisting it.
 *
 * @author yasko
 */
@Extension(target = SemanticNonPersistentPropertiesExtension.TARGET_NAME, order = 20)
public class CmfSemanticNonPersistentPropertiesProvider implements SemanticNonPersistentPropertiesExtension {

	private static final Set<String> PROPERTIES = new HashSet<>();

	static {
		PROPERTIES.add(NonPersistentProperties.LOAD_VIEW_VERSION);
		PROPERTIES.add(DefaultProperties.THUMBNAIL_IMAGE);
	}

	@Override
	public Set<String> getNonPersistentProperties() {
		return PROPERTIES;
	}

}
