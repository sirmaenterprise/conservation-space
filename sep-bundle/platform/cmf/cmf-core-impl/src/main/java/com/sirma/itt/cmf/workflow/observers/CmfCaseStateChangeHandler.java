package com.sirma.itt.cmf.workflow.observers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CaseProperties.CaseAutomaticProperties;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.event.cases.CasePersistedEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Basic cmf state change handlers.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class CmfCaseStateChangeHandler {
	@Inject
	private StateService stateService;
	/** The permission adapter service. */
	@Inject
	private CMFPermissionAdapterService permissionAdapterService;
	@Inject
	private TaskService taskService;
	@Inject
	private Logger logger;
	@Inject
	@Config(name = CmfConfigurationProperties.PERMISSION_MODEL_DMS_ENABLED, defaultValue = "false")
	private Boolean permissionModelInDMSEnabled;

	/**
	 * Handles the observable operation for state change of case.
	 *
	 * @param event
	 *            is the handled event
	 */
	public void handleOperation(@Observes OperationExecutedEvent event) {
		Operation operation = event.getOperation();
		Instance instance = event.getInstance();
		if (!(instance instanceof CaseInstance)) {
			return;
		}

		String operationType = null;
		if (operation == null) {
			return;
		}
		operationType = operation.getOperation();
		if (ActionTypeConstants.DELETE.equals(operationType)
				|| ActionTypeConstants.COMPLETE.equals(operationType)
				|| ActionTypeConstants.STOP.equals(operationType)) {
			prepareCaseWorkflowsData(instance, operationType);
		}
	}

	/**
	 * Prepare case workflows data for cancellation.
	 *
	 * @param instance
	 *            the instance
	 * @param operationType
	 *            is the operation executed
	 */
	private void prepareCaseWorkflowsData(Instance instance, String operationType) {
		HashMap<String, Serializable> map = new HashMap<String, Serializable>(2);
		map.put(CaseAutomaticProperties.AUTOMATIC_CANCEL_ACTIVE_WF, Boolean.TRUE);
		HashMap<String, Serializable> mapTaskData = new HashMap<String, Serializable>(2);
		// TODO: refactor to use state service
		if (ActionTypeConstants.DELETE.equals(operationType)) {
			mapTaskData.put(TaskProperties.STATUS,
					stateService.getState(PrimaryStates.DELETED, TaskInstance.class));
		} else {
			mapTaskData.put(TaskProperties.STATUS,
					stateService.getState(PrimaryStates.CANCELED, TaskInstance.class));
		}
		// mapTaskData.put(TaskConstants.TASK_COMMENT,
		// TaskState102Enum.TSST05_CONST);
		map.put(CaseAutomaticProperties.ACTIVE_TASKS_PROPS_UPDATE, mapTaskData);
		instance.getProperties().put(CaseAutomaticProperties.AUTOMATIC_ACTIONS_SET, map);
	}

	/**
	 * Handles the permission update for case.
	 *
	 * @param event
	 *            is the event for peristance of case
	 */
	// TODO remove when permissions are not in dms
	public void handleCasePersisted(@Observes CasePersistedEvent event) {
		updateCasePermissions(event.getInstance());
	}

	/**
	 * Update case permissions.
	 *
	 * @param caseInstance
	 *            the case instance
	 */
	private void updateCasePermissions(CaseInstance caseInstance) {
		if (Boolean.TRUE.equals(permissionModelInDMSEnabled)) {
			Map<String, Serializable> additionalProps = CollectionUtils.createHashMap(5);
			try {
				Set<String> users = taskService.getUsersWithTasksForInstance(caseInstance,
						TaskState.ALL);
				Set<String> all = CollectionUtils.createHashSet(users.size() + 4);
				all.addAll(users);
				// add admin and system user
				all.add(SecurityContextManager.getAdminUser().getIdentifier());
				all.add(SecurityContextManager.getSystemUser().getIdentifier());
				// ensure the case creator is in the list
				all.add((String) caseInstance.getProperties().get(CaseProperties.CREATED_BY));

				additionalProps.put(CMFPermissionAdapterService.LIST_OF_ALLOWED_USERS,
						(Serializable) all);

				// fetch all active currently active users
				Set<String> active = CollectionUtils.createHashSet(users.size() + 1);
				users = taskService.getUsersWithTasksForInstance(caseInstance,
						TaskState.IN_PROGRESS);
				active.addAll(users);

				additionalProps.put(CMFPermissionAdapterService.LIST_OF_ACTIVE_USERS,
						(Serializable) active);
				permissionAdapterService.updateCaseDocuments(caseInstance, additionalProps);
			} catch (DMSException e) {
				logger.error(
						"Failed to update permissions on case: "
								+ caseInstance.getContentManagementId() + " due to ", e);
			}
		}
	}
}
