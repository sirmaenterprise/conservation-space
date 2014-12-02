package com.sirma.itt.emf.state.transition;

import java.util.List;

import com.sirma.itt.emf.definition.model.Transitional;


/**
 * Definition model that defines a possible automatic state transitions
 * 
 * @author BBonev
 */
public interface StateTransitionalModel extends Transitional {
	/**
	 * Gets the state transitions.
	 * 
	 * @return the state transitions
	 */
	List<StateTransition> getStateTransitions();
}
