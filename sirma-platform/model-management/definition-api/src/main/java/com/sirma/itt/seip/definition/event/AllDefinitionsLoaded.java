package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired to notify that operation started by event started by {@link LoadAllDefinitions} has been completed
 *
 * @author BBonev
 */
@Documentation("Fired when the operation started by event <code>com.sirma.itt.emf.event.LoadAllDefinitions</code> has been completed")
public class AllDefinitionsLoaded implements EmfEvent {

	@Override
	public String toString() {
		return "AllDefinitionsLoaded";
	}

}
