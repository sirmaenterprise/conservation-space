package com.sirma.itt.seip.plugin;

import java.util.List;

/**
 * Defines a interface for implementing plugins or extension classes that can handle more operations identified by
 * something.
 *
 * @author BBonev
 * @param <T>
 *            the type of the selectors
 */
public interface Supportable<T> {

	/**
	 * Gets the list of supported objects by the extension implementation
	 *
	 * @return the supported objects
	 */
	List<T> getSupportedObjects();
}
