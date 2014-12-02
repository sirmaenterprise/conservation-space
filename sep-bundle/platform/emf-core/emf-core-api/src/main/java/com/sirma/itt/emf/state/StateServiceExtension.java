package com.sirma.itt.emf.state;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Extension point for {@link StateService}. The extension provides functionality for the service
 * for concrete instance implementation.
 * <p>
 * REVIEW change plugin to SupportablePlugin
 * 
 * @param <I>
 *            the instance type
 * @author BBonev
 */
public interface StateServiceExtension<I extends Instance> extends Plugin {

	/** Extension point name. */
	String TARGET_NAME = "stateServiceExtension";

	/**
	 * Checks if the current object can be handled by the current implementation.
	 * 
	 * @param target
	 *            the target
	 * @return true, if successful
	 */
	boolean canHandle(Object target);

	/**
	 * Changes the state of the given instance depending on the operation.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return true, if changed has occurred
	 */
	boolean changeState(I instance, Operation operation);

	/**
	 * Gets the primary state value of the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the primary state
	 */
	String getPrimaryState(I instance);

	/**
	 * Gets the state id that corresponds to the given state type and is linked to the concrete
	 * instance implementation.
	 * 
	 * @param stateType
	 *            the state type
	 * @return the state
	 */
	String getState(PrimaryStateType stateType);

	/**
	 * Checks if the given state corresponds to the given primary state type
	 * 
	 * @param stateType
	 *            the state type
	 * @param state
	 *            the state
	 * @return true, if the given state id corresponds to the given state type
	 */
	boolean isState(PrimaryStateType stateType, String state);

	/**
	 * Checks if the given instance is in the given primary state type
	 * 
	 * @param stateType
	 *            the state type
	 * @param instance
	 *            the instance
	 * @return true, if is in state
	 */
	boolean isInState(PrimaryStateType stateType, I instance);

	/**
	 * Gets the primary state codelist (required).
	 * 
	 * @return the primary state codelist
	 */
	int getPrimaryStateCodelist();

	/**
	 * Gets the secondary state codelist. Optional operation. If no codelist is set or needed the
	 * method can return <code>null</code>.
	 * 
	 * @return the secondary state codelist
	 */
	Integer getSecondaryStateCodelist();

}
