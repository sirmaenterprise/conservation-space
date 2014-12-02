package com.sirma.itt.emf.state;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service for managing case instance state operations. The implementation can fire and event of
 * type {@link com.sirma.itt.emf.state.event.StateChangedEvent} if needed.
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
	 * Gets the state id that corresponds to the given state type and is linked to the concrete
	 * instance implementation.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param stateType
	 *            the state type
	 * @param target
	 *            the target instance implementation
	 * @return the state
	 */
	<I extends Instance> String getState(PrimaryStateType stateType, Class<I> target);

	/**
	 * Checks if the given state corresponds to the given primary state type.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param stateType
	 *            the state type
	 * @param target
	 *            the target instance implementation
	 * @param state
	 *            the state
	 * @return true, if the given state id corresponds to the given state type
	 */
	<I extends Instance> boolean isState(PrimaryStateType stateType, Class<I> target, String state);

	/**
	 * Checks if the given state is any of the given primary states.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param target
	 *            the target
	 * @param state
	 *            the state
	 * @param stateType
	 *            the state type
	 * @return true, if is state as
	 */
	<I extends Instance> boolean isStateAs(Class<I> target, String state,
			PrimaryStateType... stateType);

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
	<I extends Instance> boolean isInState(PrimaryStateType stateType, I instance);

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
	<I extends Instance> boolean isInStates(I instance, PrimaryStateType... stateTypes);

	/**
	 * Gets the primary state codelist (required).
	 * 
	 * @param <I>
	 *            the generic type
	 * @param target
	 *            the target instance implementation
	 * @return the primary state codelist
	 */
	<I extends Instance> int getPrimaryStateCodelist(Class<I> target);

	/**
	 * Gets the secondary state codelist. Optional operation. If no codelist is set or needed the
	 * method can return <code>null</code>.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param target
	 *            the target the target instance implementation
	 * @return the secondary state codelist
	 */
	<I extends Instance> Integer getSecondaryStateCodelist(Class<I> target);

}
