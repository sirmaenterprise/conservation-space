package com.sirma.itt.seip.permissions.action;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.provider.MapProvider;

/**
 * Map provider for actions. Extension point for adding more {@link Action}s to the application. The provider is also an
 * extension plugin that supports ordering and overriding of the provider implementations.
 *
 * @author BBonev
 */
public interface ActionProvider extends MapProvider<String, Action>, Plugin {

	/** The target name. */
	String TARGET_NAME = "actionProviderExtension";
}
