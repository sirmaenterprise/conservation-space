package com.sirma.itt.emf.definition.event;

import com.sirma.itt.emf.event.EmfEvent;

/**
 * Event object fired to initialize all semantic definition loading. 
 * 
 * @author kirq4e
 */
public class LoadSemanticDefinitions implements EmfEvent {

	@Override
	public String toString() {
		return "LoadSemanticDefinitions";
	}

}
