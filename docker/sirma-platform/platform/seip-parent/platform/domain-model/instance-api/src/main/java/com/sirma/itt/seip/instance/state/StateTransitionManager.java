package com.sirma.itt.seip.instance.state;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;

/**
 * Manager class that can calculate the next state for a concrete instance with given current state and executed
 * operation. The manager can also provide an information for required fields.
 *
 * @author BBonev
 */
public interface StateTransitionManager {

	/**
	 * Checks if is state management supported. This means the definition for the given instance to implement the
	 * {@link com.sirma.itt.seip.definition.StateTransitionalModel} model.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if state management is supported
	 */
	boolean isStateManagementSupported(Instance instance);

	/**
	 * Gets the next state for the given instance based on the current state and the executing operation. If the method
	 * {@link #isStateManagementSupported(Instance)} returns <code>false</code> then this method will return
	 * <code>null</code>
	 *
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the next state or <code>null</code> if state management is not supported or valid state transition is not
	 *         found for the given current state and operation
	 * @see #isStateManagementSupported(Instance)
	 */
	String getNextState(Instance instance, String currentState, String operation);

	/**
	 * Gets the required fields to execute the given operation so that the instance can change his state from the given
	 * state. If the method {@link #isStateManagementSupported(Instance)} returns <code>false</code> then this method
	 * will return empty set
	 *
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the required fields or empty set if the state management is not supported or valid state transition is
	 *         not found for the given current state and operation
	 * @see #isStateManagementSupported(Instance)
	 */
	Set<String> getRequiredFields(Instance instance, String currentState, String operation);

	/**
	 * Gets the required fields expression to execute the given operation so that the instance can change his state from
	 * the given state. If the method {@link #isStateManagementSupported(Instance)} returns <code>false</code> then this
	 * method will return <code>null</code>.
	 *
	 * @param instance
	 *            the instance
	 * @param currentState
	 *            the current state
	 * @param operation
	 *            the operation
	 * @return the required fields expression or <code>null</code> if the state management is not supported or valid
	 *         state transition is not found for the given current state and operation or there is not valid expression
	 *         defined.
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
	 * Gets the allowed actions based on the definition and the current state. The returned set will contain only
	 * elements that are as the given set.
	 *
	 * @param <A>
	 *            the generic action type
	 * @param instance
	 *            instance for which to fetch the allowed actions
	 * @param currentState
	 *            the current state
	 * @param actionsAs
	 *            the maximum required actions
	 * @return the allowed actions
	 */
	<A extends Action> Set<A> getAllowedActions(Instance instance, String currentState, Set<A> actionsAs);

	/**
	 * Gets the set of allowed states that the current instance could be based on the state transition management
	 * configuration.
	 *
	 * @param instance
	 *            instance for which to fetch the allowed states
	 * @return the allowed states
	 */
	Set<String> getAllowedStates(Instance instance);

	/**
	 * Retrieves all actions that given instance supports without any filtering, except if the action does not have
	 * purpose, that kind of actions will not be present in the result set. Supports retrieving of all actions or only
	 * specified in the input set.
	 *
	 * @param identifier
	 *            the id of the instance, which actions should be returned
	 * @param actionIds
	 *            contain action ids that should be returned. If empty or <code>null</code> all found actions will be
	 *            returned
	 * @return actions for specific instance
	 */
	<A extends Action> Set<A> getActions(Serializable identifier, Set<String> actionIds);

	/**
	 * Provides list of the groups to which the actions are mapped.
	 *
	 * @param instance
	 *            instance for which to retrieve the action groups
	 * @return list of groups.
	 */
	List<TransitionGroupDefinition> getActionGroups(Instance instance);

}
