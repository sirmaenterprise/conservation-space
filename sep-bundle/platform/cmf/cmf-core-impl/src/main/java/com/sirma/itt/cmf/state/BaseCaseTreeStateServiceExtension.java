package com.sirma.itt.cmf.state;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;

/**
 * Base class for case state service managing. It is used for case, section and document state
 * management.
 * 
 * @param <I>
 *            the Instance type
 */
public abstract class BaseCaseTreeStateServiceExtension<I extends Instance> extends
		BaseStateServiceExtension<I> {

	/** The initial state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String initialState;
	/** The approved state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String approvedState;
	/** The submitted state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String submittedState;
	/** The in progress state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String inProgressState;
	/** The completed state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String completedState;
	/** The deleted state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String deletedState;
	/** The on hold state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String onHoldState;
	/** The archived state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_ARCHIVED, defaultValue = PrimaryStateType.ARCHIVED)
	private String archivedState;
	/** The cancelled state. */
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_STOPPED, defaultValue = PrimaryStateType.CANCELED)
	private String cancelledState;
	@Inject
	@Config(name = CmfConfigurationProperties.CASE_STATE_CODELIST, defaultValue = "106")
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
	public int getPrimaryStateCodelist() {
		return stateCodelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPrimaryStateProperty() {
		return DefaultProperties.STATUS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getStateTypeMapping() {
		return stateTypeMapping;
	}

}