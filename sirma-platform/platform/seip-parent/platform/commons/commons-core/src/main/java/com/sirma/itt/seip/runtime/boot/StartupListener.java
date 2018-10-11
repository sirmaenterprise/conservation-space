package com.sirma.itt.seip.runtime.boot;

/**
 * The listener interface for receiving startup events. The class that is interested in processing a startup event
 * implements this interface. When the startup event occurs, that object's appropriate method is invoked.<br>
 * The implementer is not required to implement this interface to receive startup notifications. But is required to be
 * annotated with {@link Startup} annotation.
 *
 * @author BBonev
 * @see Startup
 */
public interface StartupListener {

	/**
	 * On startup.
	 *
	 * @throws StartupException
	 *             the startup exception
	 */
	void onStartup() throws StartupException;
}
