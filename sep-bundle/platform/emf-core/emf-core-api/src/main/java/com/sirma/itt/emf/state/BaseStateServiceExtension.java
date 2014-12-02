package com.sirma.itt.emf.state;

import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Helper class for implementing concrete extensions.
 * 
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public abstract class BaseStateServiceExtension<I extends Instance> implements
		StateServiceExtension<I> {

	/** The state transition manager. */
	@Inject
	protected StateTransitionManager stateTransitionManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canHandle(Object target) {
		return (target != null)
				&& (getInstanceClass().equals(target) || getInstanceClass().isInstance(target));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getSecondaryStateCodelist() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInState(PrimaryStateType stateType, I instance) {
		String state = getPrimaryState(instance);
		return isState(stateType, state);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrimaryState(I instance) {
		if ((instance != null) && (instance.getProperties() != null)) {
			return (String) instance.getProperties().get(getPrimaryStateProperty());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isState(PrimaryStateType stateType, String state) {
		String state2 = getState(stateType);
		return EqualsHelper.nullSafeEquals(state, state2, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getState(PrimaryStateType stateType) {
		return getStateTypeMapping().get(stateType.getType());
	}

	/**
	 * Gets the state type mapping. The mapping key should be the state type ID and as value should
	 * be returned the state code
	 * 
	 * @return the state type mapping
	 */
	public abstract Map<String, String> getStateTypeMapping();

	/**
	 * Gets the primary state property.
	 * 
	 * @return the primary state property
	 */
	protected abstract String getPrimaryStateProperty();

	/**
	 * Gets the concrete implementation class.
	 * 
	 * @return the target class
	 */
	protected abstract Class<I> getInstanceClass();

	/**
	 * Tries to determine the next state automatically using the {@link StateTransitionManager} if
	 * supported.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the next state automatically or <code>null</code> if not supported
	 */
	protected String getNextStateAutomatically(I instance, String operation) {
		if (stateTransitionManager.isStateManagementSupported(instance)) {
			String status = getPrimaryState(instance);
			return stateTransitionManager.getNextState(instance, status, operation);
		}
		return null;
	}

	/**
	 * Sets the given state as primary to the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @param state
	 *            the state
	 * @return the old state
	 */
	protected String changePrimaryState(I instance, String state) {
		if (instance != null) {
			return (String) instance.getProperties().put(getPrimaryStateProperty(), state);
		}
		return null;
	}

	/**
	 * Checks for state.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	protected boolean hasState(I instance) {
		return StringUtils.isNotNullOrEmpty(getPrimaryState(instance));
	}


}
