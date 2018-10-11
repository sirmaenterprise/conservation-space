package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point for {@link StateService}. The extension provides functionality for the service for concrete instance
 * implementation.
 *
 * @author BBonev
 */
public interface StateServiceExtension extends Plugin {

	/** Extension point name. */
	String TARGET_NAME = "stateServiceExtension";

	/**
	 * Checks if the current object type can be handled by the current implementation.
	 *
	 * @param targetType
	 *            the type of the instance that need to be handled
	 * @return true, if successful
	 */
	boolean canHandle(String targetType);

	/**
	 * Changes the state of the given instance depending on the operation.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return true, if changed has occurred
	 */
	boolean changeState(Instance instance, Operation operation);

	/**
	 * Gets the primary state value of the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the primary state
	 */
	String getPrimaryState(Instance instance);

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
	boolean isInState(PrimaryStates stateType, Instance instance);

	/**
	 * Checks if is in active state.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is in active state
	 */
	boolean isInActiveState(Instance instance);

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
