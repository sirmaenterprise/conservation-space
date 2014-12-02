package com.sirma.itt.emf.event;

import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that the application has been initialized to a state when other services
 * that need to be started on application start without being Singleton beans could start loading.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that the application has been initialized to a state when other services that need to be started on application start without being Singleton beans could start loading.")
public class ApplicationStartupEvent implements EmfEvent {

	/**
	 * Instantiates a new application startup.
	 */
	public ApplicationStartupEvent() {
		// nothing to do here
	}

}
