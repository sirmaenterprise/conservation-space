package com.sirma.cmf.web.workflow.task;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.entity.dispatcher.EntityOpenDispatcher;
import com.sirma.cmf.web.workflow.WorkflowActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.security.model.User;

/**
 * Executor for workflow transition actions. The transitions are retrievable from the definitions.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowTransitionExecutor extends WorkflowActionBase {

	private static final long serialVersionUID = 3520657387420410323L;

	/** Transition identifier for start workflow. */
	private static final String TRANSITION_ID_START_WORKFLOW = "startworkflow";

	/** Transition identifier for cancel start. */
	private static final String TRANSIITON_ID_CANCEL_START = "cancelstart";

	@Inject
	private EntityOpenDispatcher entityOpenDispatcher;

	/**
	 * Execute transition action. This is the default action that is called by the workflow task
	 * operation buttons.
	 * 
	 * @param transitionId
	 *            the transition id
	 * @return Navigation string
	 */
	public String executeTransition(String transitionId) {
		log.debug("CMFWeb: WorkflowTaskAction.executeTransition with transitionId [" + transitionId
				+ "]");
		String navigationString = null;
		if (StringUtils.isNotNullOrEmpty(transitionId)) {
			String transitionLowerCase = transitionId.toLowerCase();
			// execute transition start workflow
			if (TRANSITION_ID_START_WORKFLOW.equals(transitionLowerCase)) {
				navigationString = startWorkflow(transitionId);
				// execute transition cancel start
			} else if (TRANSIITON_ID_CANCEL_START.equals(transitionLowerCase)) {
				navigationString = NavigationConstants.BACKWARD;
				// execute not specified transition
			} else {
				navigationString = doTransition(transitionId);
			}
			reloadCaseInstance();
		}
		return navigationString;
	}

	/**
	 * Do any transition.
	 * 
	 * @param transitionId
	 *            the transition id
	 * @return the string
	 */
	private String doTransition(final String transitionId) {
		WorkflowInstanceContext workflowInstance = getDocumentContext().getInstance(
				WorkflowInstanceContext.class);
		TaskInstance taskInstance = getDocumentContext().getInstance(TaskInstance.class);
		instanceService.refresh(workflowInstance);
		List<TaskInstance> nextTasks = workflowService.updateWorkflow(workflowInstance,
				taskInstance, transitionId);
		TaskInstance myTask = getMyTask(nextTasks, currentUser);
		if (myTask != null) {
			log.debug("CMFWeb: WorkflowTransitionExecutor.openMyTask - open a task assigned to current user.");
			entityOpenDispatcher.openInternal(myTask.getClass().getSimpleName().toLowerCase(),
					(String) myTask.getId(), null);
			return NavigationConstants.NAVIGATE_TASK_DETAILS_PAGE;
		}

		// going to case dashboard
		CaseInstance parentCase = InstanceUtil.getParent(CaseInstance.class, taskInstance);
		entityOpenDispatcher.openInternal(parentCase.getClass().getSimpleName().toLowerCase(),
				(String) parentCase.getId(), null);
		return NavigationConstants.NAVIGATE_TAB_CASE_DASHBOARD;
	}

	/**
	 * Execute start workflow action.
	 * 
	 * @param transitionId
	 *            the transition id
	 * @return the string
	 */
	private String startWorkflow(final String transitionId) {
		log.debug("CMFWeb: WorkflowTaskAction.executeStartWorkflowAction with transitionId ["
				+ transitionId + "]");

		WorkflowInstanceContext workflowInstance = getDocumentContext().getInstance(
				WorkflowInstanceContext.class);

		TaskInstance startTaskInstance = getDocumentContext().getInstance(TaskInstance.class);
		List<TaskInstance> nextTasks = workflowService.startWorkflow(workflowInstance,
				startTaskInstance);
		TaskInstance myTask = openMyTask(nextTasks);
		if (myTask != null) {
			return NavigationConstants.NAVIGATE_TASK_DETAILS_PAGE;
		}

		return NavigationConstants.BACKWARD;
	}

	/**
	 * If there is one task assigned to current user, then it is opened by default.<br>
	 * <b>If a task is found, then explicit navigation is performed trough navigation handler and
	 * the code after this method won't be executed.</b>
	 * 
	 * @param nextTasks
	 *            the next tasks
	 * @return the task instance
	 */
	private TaskInstance openMyTask(List<TaskInstance> nextTasks) {
		TaskInstance myTask = getMyTask(nextTasks, currentUser);
		if (myTask != null) {
			log.debug("CMFWeb: WorkflowTransitionExecutor.openMyTask - open a task assigned to current user.");
			entityOpenDispatcher.openInternal(myTask.getClass().getSimpleName().toLowerCase(),
					(String) myTask.getId(), null);
		}
		return myTask;
	}

	/**
	 * Getter for next task assigned to current user.
	 * 
	 * @param nextTasks
	 *            the next tasks
	 * @param currentUser
	 *            the current user
	 * @return the my task
	 */
	private TaskInstance getMyTask(List<TaskInstance> nextTasks, User currentUser) {
		TaskInstance foundTask = null;
		if (nextTasks != null) {
			for (TaskInstance taskInstance : nextTasks) {
				String userId = (String) taskInstance.getProperties()
						.get(TaskProperties.TASK_OWNER);
				if ((userId != null) && currentUser.getName().equals(userId)) {
					foundTask = taskInstance;
					break;
				}
			}
		}
		return foundTask;
	}
}
