package com.sirma.itt.emf.definition.model;

import java.util.List;

import com.sirma.itt.emf.domain.model.Identity;

/**
 * Common interface for definitions with transitions.
 * 
 * @author BBonev
 */
public interface Transitional extends Identity {
	/**
	 * Getter method for transitions.
	 * 
	 * @return the transitions
	 */
	List<TransitionDefinition> getTransitions();
}
