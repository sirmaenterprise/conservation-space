package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired to notify that the definitions import process has finished.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that the definitions were updated")
public class DefinitionsChangedEvent implements EmfEvent {

	@Override
	public String toString() {
		return "DefinitionsChangedEvent";
	}

}
