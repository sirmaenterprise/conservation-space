package com.sirma.itt.emf.state;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfStateConfigurationProperties;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Abstract state service extension that defines the default generic state mappings and to provide
 * the default change state logic that uses the
 * {@link com.sirma.itt.emf.state.transition.StateTransitionManager} for the next state
 * 
 * @param <I>
 *            the generic type
 * @author BBonev
 */
public abstract class GenericInstanceStateServiceExtension<I extends Instance> extends
		BaseStateServiceExtension<I> {

	/** The initial state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String initialState;
	/** The approved state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String approvedState;
	/** The submitted state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String submittedState;
	/** The in progress state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String inProgressState;
	/** The completed state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String completedState;
	/** The deleted state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String deletedState;
	/** The on hold state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String onHoldState;
	/** The archived state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_ARCHIVED, defaultValue = PrimaryStateType.ARCHIVED)
	private String archivedState;
	/** The cancelled state. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_STOPPED, defaultValue = PrimaryStateType.CANCELED)
	private String cancelledState;

	/** The state codelist. */
	@Inject
	@Config(name = EmfStateConfigurationProperties.DEFAULT_STATE_CODELIST, defaultValue = "11")
	private Integer stateCodelist;

	/** The state type mapping. */
	private Map<String, String> stateTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		// moved to external configuration
		stateTypeMapping = new LinkedHashMap<String, String>();
		stateTypeMapping.put(PrimaryStateType.INITIAL, initialState);
		stateTypeMapping.put(PrimaryStateType.ARCHIVED, archivedState);
		stateTypeMapping.put(PrimaryStateType.CANCELED, cancelledState);
		stateTypeMapping.put(PrimaryStateType.COMPLETED, completedState);
		stateTypeMapping.put(PrimaryStateType.DELETED, deletedState);
		stateTypeMapping.put(PrimaryStateType.ON_HOLD, onHoldState);
		stateTypeMapping.put(PrimaryStateType.IN_PROGRESS, inProgressState);
		stateTypeMapping.put(PrimaryStateType.SUBMITTED, submittedState);
		stateTypeMapping.put(PrimaryStateType.APPROVED, approvedState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(I instance, Operation operation) {
		String operationType = null;
		if (operation != null) {
			operationType = operation.getOperation();
		}

		String nextStateAutomatically = getNextStateAutomatically(instance, operationType);
		if (StringUtils.isNotNullOrEmpty(nextStateAutomatically)) {
			String string = changePrimaryState(instance, nextStateAutomatically);
			return !EqualsHelper.nullSafeEquals(string, nextStateAutomatically);
		}

		return false;
	}

	@Override
	public int getPrimaryStateCodelist() {
		return stateCodelist;
	}

	@Override
	public Map<String, String> getStateTypeMapping() {
		return stateTypeMapping;
	}

	@Override
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

}
