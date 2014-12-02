package com.sirma.itt.emf.security.evaluator;

import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.dao.SemanticNonPersistentPropertiesExtension;

/**
 * Provider for the property keys that should be removed from an instance when persisting it.
 * 
 * @author BBonev
 */
@Extension(target = SemanticNonPersistentPropertiesExtension.TARGET_NAME, order = 10)
public class EmfSemanticNonPersistentPropertiesProvider implements
		SemanticNonPersistentPropertiesExtension {

	/** The Constant PROPERTIES. */
	private static final Set<String> PROPERTIES = new HashSet<>();

	static {
		PROPERTIES.add(DefaultProperties.EVALUATED_ACTIONS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getNonPersistentProperties() {
		return PROPERTIES;
	}

}
