package com.sirma.itt.emf.state.transition;

import java.util.Set;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;

/**
 * Manager class that can calculate the next state for a concrete instance with given current state
 * and executed operation. The manager can also provide an information for required fields.
 * 
 * @author BBonev
 */
public interface StateTransitionManager {

	/**
	 * Checks if is state management supported. This means the definition for the given instance to
	 * implement the {@link com.sirma.itt.emf.state.transition.StateTransitionalModel} model.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if state management is supported
	 */
	boolean isStateManagementSupported(Instance instance);

	/**
	 * Gets the next state for the given instance based on the current state and the executing
	 * operation. If the method {@link #isStateManagementSupported(Instance)} returns
	 * <code>false</code> then this method will return <code>null</code>
	 * 
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the next state or <code>null</code> if state management is not supported or valid
	 *         state transition is not found for the given current state and operation
	 * @see #isStateManagementSupported(Instance)
	 */
	String getNextState(Instance instance, String currentState, String operation);

	/**
	 * Gets the required fields to execute the given operation so that the instance can change his
	 * state from the given state. If the method {@link #isStateManagementSupported(Instance)}
	 * returns <code>false</code> then this method will return empty set
	 * 
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the required fields or empty set if the state management is not supported or valid
	 *         state transition is not found for the given current state and operation
	 * @see #isStateManagementSupported(Instance)
	 */
	Set<String> getRequiredFields(Instance instance, String currentState, String operation);

	/**
	 * Gets the required fields expression to execute the given operation so that the instance can
	 * change his
	 * state from the given state. If the method {@link #isStateManagementSupported(Instance)}
	 * returns <code>false</code> then this method will return <code>null</code>.
	 * 
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the required fields expression or <code>null</code> if the state management is not
	 *         supported or valid
	 *         state transition is not found for the given current state and operation or there is
	 *         not valid expression defined.
	 * @see #isStateManagementSupported(Instance)
	 */
	String getRequiredFieldsExpression(Instance instance, String currentState, String operation);

	/**
	 * Gets the allowed operations for the given state.
	 * 
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @return the allowed operations
	 */
	Set<String> getAllowedOperations(Instance instance, String currentState);

	/**
	 * Gets the allowed actions based on the definition and the current state
	 * 
	 * @param <A>
	 *            the generic action type
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @return the allowed actions
	 */
	<A extends Action> Set<A> getAllowedActions(Instance instance, String currentState);

	/**
	 * Gets the allowed actions based on the definition and the current state. The returned set will
	 * contain only elements that are as the given set.
	 * 
	 * @param <A>
	 *            the generic action type
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param actionsAs
	 *            the maximum required actions.
	 * @return the allowed actions
	 */
	<A extends Action> Set<A> getAllowedActions(Instance instance, String currentState,
			Set<A> actionsAs);

	/**
	 * Gets the set of allowed states that the current instance could be based on the state
	 * transition management configuration.
	 * 
	 * @param instance
	 *            the instance
	 * @return the allowed states
	 */
	Set<String> getAllowedStates(Instance instance);
}
