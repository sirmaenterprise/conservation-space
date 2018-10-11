package com.sirma.itt.seip.monitor;

import com.sirma.itt.seip.provider.Provider;

/**
 * Extension to allow external module integrations to add means to create {@link Statistics} implementation. The
 * provider should be registered using Java Service Provider Interface (SPI).
 * <p>
 * The module should define a file META-INF/services/com.sirma.itt.emf.monitor.StatisticsProvider and define the
 * implementation class to be loaded and used for object creation.
 *
 * @author BBonev
 */
public interface StatisticsProvider extends Provider<Statistics> {
	// nothing to add
}
