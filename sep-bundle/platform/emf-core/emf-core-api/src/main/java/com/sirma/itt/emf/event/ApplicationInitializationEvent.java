package com.sirma.itt.emf.event;

import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that the application is beginning initialiation. This event is fired for
 * services that need to be initialized before the rest of the application should start loading.
 * Generally The database is initialzied and updated to the lastest patch.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that the application is beginning initialiation. This event is fired for services that need to be initialized before the rest of the application should start loading. Generally The database is initialzied and updated to the lastest patch.")
public class ApplicationInitializationEvent implements EmfEvent {

	/**
	 * Instantiates a new application initialization event.
	 */
	public ApplicationInitializationEvent() {
		// nothing to do here
	}

}
