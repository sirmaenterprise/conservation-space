package com.sirma.itt.seip.plugin;

import java.util.List;

/**
 * Represents an application plugin that supports only a specific set of classes. Defines an extension that can handle
 * only the classes returned by the method {@link #getSupportedObjects()}.
 *
 * @param <T> the type of the selector returned by the {@link #getSupportedObjects()}
 * @author BBonev
 */
public interface SupportablePlugin<T> extends Plugin, Supportable<T> {

	/**
	 * Gets the list of supported objects by the extension implementation
	 *
	 * @return the supported objects
	 */
	@Override
	List<T> getSupportedObjects();
}
