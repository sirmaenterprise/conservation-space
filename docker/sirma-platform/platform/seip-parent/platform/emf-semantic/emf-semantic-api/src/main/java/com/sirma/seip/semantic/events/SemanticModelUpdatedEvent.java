package com.sirma.seip.semantic.events;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Used to notify that semantic model is updated.
 *
 * @author A. Kunchev
 */
@Documentation("Used to notify that semantic model is updated.")
public class SemanticModelUpdatedEvent implements EmfEvent {

	@Override
	public String toString() {
		return "SemanticModelUpdatedEvent";
	}
}
