package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.Conditional;

/**
 * Defines the interface to describe the possible state transitions and the optional conditions to navigate from one
 * state to another for concrete transition.
 *
 * @author BBonev
 */
public interface StateTransition extends Conditional, Identity {

	/**
	 * Gets the from state.
	 *
	 * @return the from state
	 */
	String getFromState();

	/**
	 * Gets the transition id.
	 *
	 * @return the transition id
	 */
	String getTransitionId();

	/**
	 * Gets the to state.
	 *
	 * @return the to state
	 */
	String getToState();

}
