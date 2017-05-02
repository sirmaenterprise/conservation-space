package com.sirma.itt.seip.plugin;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.provider.Provider;

/**
 * Plugin that can define service chains. The implementations should provide the concrete service implementations and
 * the extensions should be in the order of requirement.
 *
 * @author BBonev
 * @param <E>
 *            the service type
 */
@Documentation("Plugin that can define service chains. The implementations should provide the concrete service implementations and the extensions should be in the order of requirement.")
public interface ServiceChainProvider<E> extends Plugin, Provider<E> {
	// nothing to add
}
