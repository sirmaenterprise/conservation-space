package com.sirma.itt.emf.definition.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that operation started by event started by {@link LoadAllDefinitions} has
 * been completed
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
