package com.sirma.itt.cmf.state;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Extension implementation for workflow state management.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Documentation("Extension implementation for workflow state management")
@InstanceType(type = ObjectTypesCmf.WORKFLOW)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 20)
public class WorkflowStateServiceExtension extends
		BaseStateServiceExtension<WorkflowInstanceContext> {

	/** The workflow status codelist. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_CODELIST, defaultValue = "101")
	private Integer workflowStatusCodelist;

	/** The workflow state initial. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_INITIAL, defaultValue = PrimaryStateType.INITIAL)
	private String workflowStateInitial;
	/** The workflow state approved. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_APPROVED, defaultValue = PrimaryStateType.APPROVED)
	private String workflowStateApproved;
	/** The workflow state submitted. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_SUBMITTED, defaultValue = PrimaryStateType.SUBMITTED)
	private String workflowStateSubmitted;
	/** The workflow state on hold. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_ON_HOLD, defaultValue = PrimaryStateType.ON_HOLD)
	private String workflowStateOnHold;
	/** The workflow state deleted. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_DELETED, defaultValue = PrimaryStateType.DELETED)
	private String workflowStateDeleted;
	/** The workflow state in progress. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_IN_PROGRESS, defaultValue = PrimaryStateType.IN_PROGRESS)
	private String workflowStateInProgress;
	/** The workflow state completed. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_COMPLETED, defaultValue = PrimaryStateType.COMPLETED)
	private String workflowStateCompleted;
	/** The workflow state canceled. */
	@Inject
	@Config(name = CmfConfigurationProperties.WORKFLOW_STATE_CANCELED, defaultValue = PrimaryStateType.CANCELED)
	private String workflowStateCanceled;

	/** The state type mapping. */
	private Map<String, String> stateTypeMapping;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		stateTypeMapping = new LinkedHashMap<String, String>();
		stateTypeMapping.put(PrimaryStateType.INITIAL, workflowStateInitial);
		stateTypeMapping.put(PrimaryStateType.ARCHIVED, workflowStateCompleted);
		stateTypeMapping.put(PrimaryStateType.CANCELED, workflowStateCanceled);
		stateTypeMapping.put(PrimaryStateType.COMPLETED, workflowStateCompleted);
		stateTypeMapping.put(PrimaryStateType.DELETED, workflowStateDeleted);
		stateTypeMapping.put(PrimaryStateType.ON_HOLD, workflowStateOnHold);
		stateTypeMapping.put(PrimaryStateType.IN_PROGRESS, workflowStateInProgress);
		stateTypeMapping.put(PrimaryStateType.SUBMITTED, workflowStateSubmitted);
		stateTypeMapping.put(PrimaryStateType.APPROVED, workflowStateApproved);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(WorkflowInstanceContext instance, Operation operation) {
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
		return workflowStatusCodelist;
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
		return WorkflowProperties.STATUS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<WorkflowInstanceContext> getInstanceClass() {
		return WorkflowInstanceContext.class;
	}
}
