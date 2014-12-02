package com.sirma.itt.emf.definition.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that operation started by event {@link LoadTopLevelDefinitions} has been
 * completed
 * 
 * @author BBonev
 */
@Documentation("Fired when the operation started by event <code>com.sirma.itt.emf.event.LoadTopLevelDefinitions</code> has been completed")
public class TopLevelDefinitionsLoaded implements EmfEvent {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "TopLevelDefinitionsLoaded";
	}

}
