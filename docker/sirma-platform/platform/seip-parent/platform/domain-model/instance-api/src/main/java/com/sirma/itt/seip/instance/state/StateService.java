package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Service for managing case instance state operations. The implementation can fire and event of type
 * {@link com.sirma.itt.seip.instance.state.StateChangedEvent} if needed.
 *
 * @author BBonev
 */
public interface StateService {

	/**
	 * Changes the state of the given instance depending on the operation.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	<I extends Instance> void changeState(I instance, Operation operation);

	/**
	 * Gets the primary state value of the given instance.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the primary state
	 */
	<I extends Instance> String getPrimaryState(I instance);

	/**
	 * Gets the state id that corresponds to the given state type and is linked to the concrete instance implementation.
	 *
	 * @param stateType
	 *            the state type
	 * @param targetType
	 *            the category of the instance type
	 * @return the state
	 */
	String getState(PrimaryStates stateType, String targetType);

	/**
	 * Checks if the given state corresponds to the given primary state type.
	 *
	 * @param stateType
	 *            the state type
	 * @param targetType
	 *            the category of the instance type
	 * @param state
	 *            the state
	 * @return true, if the given state id corresponds to the given state type
	 */
	boolean isState(PrimaryStates stateType, String targetType, String state);

	/**
	 * Checks if the given state is any of the given primary states.
	 *
	 * @param targetType
	 *            the category of the instance type
	 * @param state
	 *            the state
	 * @param stateType
	 *            the state type
	 * @return true, if is state as
	 */
	boolean isStateAs(String targetType, String state, PrimaryStates... stateType);

	/**
	 * Checks if the given instance is in the given primary state type.
	 *
	 * @param <I>
	 *            the generic type
	 * @param stateType
	 *            the state type
	 * @param instance
	 *            the instance
	 * @return true, if is in state
	 */
	<I extends Instance> boolean isInState(PrimaryStates stateType, I instance);

	/**
	 * Checks if the given instance is in any of the given states
	 *
	 * @param <I>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @param stateTypes
	 *            the state types
	 * @return true, if is in states
	 */
	<I extends Instance> boolean isInStates(I instance, PrimaryStates... stateTypes);

	/**
	 * Checks if is in active state.
	 *
	 * @param <I>
	 *            instance type
	 * @param instance
	 *            the instance
	 * @return true, if is in active state
	 */
	<I extends Instance> boolean isInActiveState(I instance);

	/**
	 * Gets the primary state codelist (required).
	 *
	 * @param targetType
	 *            the category of the instance type
	 * @return the primary state codelist
	 */
	int getPrimaryStateCodelist(String targetType);

	/**
	 * Gets the secondary state codelist. Optional operation. If no codelist is set or needed the method can return
	 * <code>null</code>.
	 *
	 * @param targetType
	 *            the category of the instance type
	 * @return the secondary state codelist
	 */
	Integer getSecondaryStateCodelist(String targetType);

}
