package com.sirma.itt.seip.provider;

/**
 * Base provider class that provides some concrete information. The information from all providers are collected and can
 * be accessed from the provider registry.
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
@FunctionalInterface
public interface Provider<E> {

	/**
	 * Provides the data from the given data.
	 *
	 * @return the data
	 */
	E provide();
}
