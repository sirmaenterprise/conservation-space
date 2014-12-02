package com.sirma.itt.emf.plugin;

import java.util.List;

/**
 * Represents an application plugin that supports only a specific set of classes. Defines an
 * extension that can handle only the classes returned by the method {@link #getSupportedObjects()}.
 * 
 * @author BBonev
 */
public interface SupportablePlugin extends Plugin, Supportable<Class<?>> {

	/**
	 * Gets the list of supported objects by the extension implementation
	 * 
	 * @return the supported objects
	 */
	@Override
	List<Class<?>> getSupportedObjects();
}
