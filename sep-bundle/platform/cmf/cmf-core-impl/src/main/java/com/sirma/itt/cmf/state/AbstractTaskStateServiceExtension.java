package com.sirma.itt.cmf.state;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Basic state service extension for tasks handling
 * 
 * @param <E>
 *            the task type
 */
public abstract class AbstractTaskStateServiceExtension<E extends AbstractTaskInstance> extends
		BaseStateServiceExtension<E> {

	/** The task status codelist. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_CODELIST, defaultValue = "102")
	private Integer taskStatusCodelist;
	/** The task state initial. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String taskStateInitial;
	/** The task state submitted. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String taskStateSubmitted;
	/** The task state deleted. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String taskStateDeleted;
	/** The task state in progress. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String taskStateInProgress;
	/** The task state open. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String taskStateApproved;
	/** The task state on hold. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String taskStateOnHold;
	/** The task state completed. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String taskStateCompleted;
	/** The task state canceled. */
	@Inject
	@Config(name = CmfConfigurationProperties.TASK_STATE_CANCELED, defaultValue = PrimaryStateType.CANCELED)
	private String taskStateCanceled;
	/** The state type mapping. */
	private Map<String, String> stateTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		stateTypeMapping = new LinkedHashMap<String, String>();
		stateTypeMapping.put(PrimaryStateType.INITIAL, taskStateInitial);
		stateTypeMapping.put(PrimaryStateType.ARCHIVED, taskStateCompleted);
		stateTypeMapping.put(PrimaryStateType.CANCELED, taskStateCanceled);
		stateTypeMapping.put(PrimaryStateType.COMPLETED, taskStateCompleted);
		stateTypeMapping.put(PrimaryStateType.DELETED, taskStateDeleted);
		stateTypeMapping.put(PrimaryStateType.ON_HOLD, taskStateOnHold);
		stateTypeMapping.put(PrimaryStateType.IN_PROGRESS, taskStateInProgress);
		stateTypeMapping.put(PrimaryStateType.SUBMITTED, taskStateSubmitted);
		stateTypeMapping.put(PrimaryStateType.APPROVED, taskStateApproved);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(E instance, Operation operation) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPrimaryStateCodelist() {
		return taskStatusCodelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getStateTypeMapping() {
		return stateTypeMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPrimaryStateProperty() {
		return TaskProperties.STATUS;
	}

}