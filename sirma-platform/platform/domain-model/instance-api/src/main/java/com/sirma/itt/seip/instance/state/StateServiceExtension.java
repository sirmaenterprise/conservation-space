package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point for {@link StateService}. The extension provides functionality for the service for concrete instance
 * implementation.
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
	 * Gets the state id that corresponds to the given state type and is linked to the concrete instance implementation.
	 *
	 * @param stateType
	 *            the state type
	 * @return the state
	 */
	String getState(PrimaryStates stateType);

	/**
	 * Checks if the given state corresponds to the given primary state type
	 *
	 * @param stateType
	 *            the state type
	 * @param state
	 *            the state
	 * @return true, if the given state id corresponds to the given state type
	 */
	boolean isState(PrimaryStates stateType, String state);

	/**
	 * Checks if the given instance is in the given primary state type
	 *
	 * @param stateType
	 *            the state type
	 * @param instance
	 *            the instance
	 * @return true, if is in state
	 */
	boolean isInState(PrimaryStates stateType, I instance);

	/**
	 * Checks if is in active state.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is in active state
	 */
	boolean isInActiveState(I instance);

	/**
	 * Gets the primary state codelist (required).
	 *
	 * @return the primary state codelist
	 */
	int getPrimaryStateCodelist();

	/**
	 * Gets the secondary state codelist. Optional operation. If no codelist is set or needed the method can return
	 * <code>null</code>.
	 *
	 * @return the secondary state codelist
	 */
	Integer getSecondaryStateCodelist();

}
