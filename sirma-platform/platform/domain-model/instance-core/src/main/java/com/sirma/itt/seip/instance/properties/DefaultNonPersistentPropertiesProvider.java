package com.sirma.itt.seip.instance.properties;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provider for the base and standard properties that should not be persisted.
 *
 * @author bbanchev
 */
@Extension(target = SemanticNonPersistentPropertiesExtension.TARGET_NAME, order = 5)
public class DefaultNonPersistentPropertiesProvider implements SemanticNonPersistentPropertiesExtension {

	private static final Set<String> PROPERTIES = new HashSet<>(1);

	static {
		PROPERTIES.add(DefaultProperties.ENTITY_IDENTIFIER);
	}

	@Override
	public Set<String> getNonPersistentProperties() {
		return PROPERTIES;
	}

}
