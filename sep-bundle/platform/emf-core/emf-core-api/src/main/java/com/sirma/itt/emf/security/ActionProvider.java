package com.sirma.itt.emf.security;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.provider.MapProvider;
import com.sirma.itt.emf.security.model.Action;

/**
 * Map provider for actions. Extension point for adding more {@link Action}s to the application. The
 * provider is also an extension plugin that supports ordering and overriding of the provider
 * implementations.
 * 
 * @author BBonev
 */
public interface ActionProvider extends MapProvider<Pair<Class<?>, String>, Action>, Plugin {

	/** The target name. */
	String TARGET_NAME = "actionProviderExtension";
}
