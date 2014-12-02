package com.sirma.cmf.web.workflow.task;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.SelectorItem;
import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.workflow.DrawBean;
import com.sirma.cmf.web.workflow.WorkflowActionBase;
import com.sirma.cmf.web.workflow.WorkflowTasksHolder;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskActivateEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskActivateEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskClaimEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOnHoldEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOpenEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskReleaseEvent;
import com.sirma.itt.cmf.security.evaluator.TaskRoleEvaluator;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.emf.web.action.event.EmfImmediateActionEvent;

/**
 * Implementations for page initialization and actions observers.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowTaskAction extends WorkflowActionBase {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9098569180836928304L;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The workflow tasks holder. */
	@Inject
	private WorkflowTasksHolder workflowTasksHolder;

	/** The users list. */
	private List<SelectorItem> usersList;

	/** The new assignee id. */
	private String newAssigneeId;

	/** The items. */
	private String items;

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** Workflow diagram drawing bean. */
	@Inject
	private DrawBean drawBean;

	/** Key to process diagram buffered image in session map. */
	private String picKey;

	/** If current user has edit permission for the task. */
	private boolean hasEditPermission;

	/**
	 * Initializes the task details page.
	 */
	public void initTaskDetailsPage() {
		initTaskPropertiesForm();

		initTaskForm(TaskProperties.PURPOSE_TASK_METADATA, CMFConstants.TASK_COMMON_DATA_PANEL,
				FormViewMode.PREVIEW);

		initTaskForm(TaskProperties.PURPOSE_WORKFLOW_HISTORY, CMFConstants.WORKFLOW_HISTORY_PANEL,
				FormViewMode.PREVIEW);

		TaskDefinitionRef taskDefinition = getDocumentContext().getDefinition(
				TaskDefinitionRef.class);
		AbstractTaskInstance taskInstance = getDocumentContext().getInstance(TaskInstance.class);
		WorkflowInstanceContext workflowInstanceContext = (WorkflowInstanceContext) taskInstance
				.getOwningInstance();
		FormViewMode formViewMode = hasEditPermissionForTask(taskInstance);
		if (getTransitionActions() != null) {
			getTransitionActions().clear();
		}
		// render transition buttons only if wf is active
		if (taskInstance.isEditable() && workflowInstanceContext.isActive()
				&& (formViewMode == FormViewMode.EDIT)) {
			setTransitionActions(buildTransitionActions(taskDefinition));
		}

		hasEditPermission = canEditTask(formViewMode);

		// REVIEW: load in parallel
		List<TaskInstance> completedTasks = loadTasksByWorkflow(workflowInstanceContext);
		workflowTasksHolder.setCompletedTasks(completedTasks);

		// load workflow diagram
		loadProcessDiagram(workflowInstanceContext);
	}

	/**
	 * Inits the task form.
	 * 
	 * @param taskId
	 *            the task id
	 * @param panelId
	 *            the panel id
	 * @param formViewMode
	 *            the form view mode
	 */
	private void initTaskForm(String taskId, String panelId, FormViewMode formViewMode) {

		log.debug("Executing WorkflowTaskAction.initTaskForm for taskId[" + taskId + "]");

		TaskInstance taskInstance = getDocumentContext().getInstance(TaskInstance.class);

		String workflowDefinitionId = taskInstance.getContext().getIdentifier();

		WorkflowDefinition workflowDefinition = dictionaryService.getDefinition(
				WorkflowDefinition.class, workflowDefinitionId, taskInstance.getRevision());

		TaskDefinitionRef taskDefinition = WorkflowHelper.getTaskByPurpose(workflowDefinition,
				taskId);

		UIComponent panel = RichFunction.findComponent(panelId);

		if (panel != null) {
			panel.getChildren().clear();
		}

		invokeReader(taskDefinition, taskInstance, panel, FormViewMode.PREVIEW, null);
	}

	/**
	 * Initializes the task properties form.
	 */
	public void initTaskPropertiesForm() {

		log.debug("WorkflowTaskAction.initTaskPropertiesForm");

		TaskDefinitionRef taskDefinition = getDocumentContext().getDefinition(
				TaskDefinitionRef.class);
		AbstractTaskInstance taskInstance = getDocumentContext().getInstance(TaskInstance.class);

		WorkflowInstanceContext workflowInstanceContext = (WorkflowInstanceContext) taskInstance
				.getOwningInstance();

		UIComponent panel = RichFunction.findComponent(CMFConstants.TASK_DATA_PANEL);

		if ((workflowInstanceContext != null)
				&& SequenceEntityGenerator.isPersisted(workflowInstanceContext)) {
			// there was a check if action is invoked as a result of ajax request but it seems that
			// this is not a problem at the moment
			// boolean ajaxRequest = isAjaxRequest();
			boolean validationFailed = FacesContext.getCurrentInstance().isValidationFailed();
			if (!validationFailed) {
				if (panel != null) {
					panel.getChildren().clear();
				}

				FormViewMode formViewMode = hasEditPermissionForTask(taskInstance);
				// - build the form
				// - pass the taskInstance instead of workflowInstance when we want
				// to render the task data
				invokeReader(taskDefinition, taskInstance, panel, formViewMode, null);
			}
		}
	}

	/**
	 * Renders current process diagram.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return string as value for a4j:mediaOutput UI component
	 */
	public String loadProcessDiagram(WorkflowInstanceContext workflowInstanceContext) {

		String stringKey = null;
		log.debug("Executing WorkflowDetails.loadProcessDiagram");

		if (workflowInstanceContext != null) {
			BufferedImage bufferedImage = workflowService
					.getWorkflowProcessDiagram(workflowInstanceContext);

			stringKey = "tst" + getWorkflowIdentifier(workflowInstanceContext);
			setPicKey(stringKey);

			FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
					.put(getPicKey(), bufferedImage);

			if (bufferedImage == null) {
				drawBean.setMissingImage(true);
			} else {
				drawBean.setMissingImage(false);
			}
		}
		return stringKey;
	}

	/**
	 * Gets the workflow identifier.
	 * 
	 * @param workflowInstanceContext
	 *            the workflow instance context
	 * @return the workflow identifier
	 */
	public String getWorkflowIdentifier(WorkflowInstanceContext workflowInstanceContext) {
		return workflowInstanceContext.getId().toString().replace(':', '-');
	}

	/**
	 * Checks if is task form editable.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return true, if is task form editable
	 */
	public boolean isTaskFormEditable(AbstractTaskInstance taskInstance) {
		return hasEditPermissionForTask(taskInstance) == FormViewMode.EDIT;
	}

	/**
	 * Checks if is task form editable.
	 * 
	 * @param mode
	 *            the mode
	 * @return true, if is task form editable
	 */
	private boolean canEditTask(FormViewMode mode) {
		return mode == FormViewMode.EDIT;
	}

	/**
	 * Decide the form view mode.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the form view mode
	 */
	private FormViewMode hasEditPermissionForTask(AbstractTaskInstance taskInstance) {
		Set<Action> allowedActions = authorityService.getAllowedActions(taskInstance, "");
		if (allowedActions.contains(TaskRoleEvaluator.EDIT)) {
			return FormViewMode.EDIT;
		}
		return FormViewMode.PREVIEW;
	}

	/**
	 * This method will gather all data for populating the form, for editing task.
	 * 
	 * @param event
	 *            current event
	 */
	public void editTask(
			@Observes @EMFAction(value = ActionTypeConstants.EDIT_DETAILS, target = TaskInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.editTask");
		TaskInstance taskInstance = (TaskInstance) event.getInstance();
		TaskDefinitionRef taskDefinition = (TaskDefinitionRef) dictionaryService
				.getInstanceDefinition(taskInstance);
		eventService.fire(new TaskOpenEvent(taskInstance));
		// populate task form for editing
		getDocumentContext().populateContext(taskInstance, TaskDefinitionRef.class, taskDefinition);
		getDocumentContext().addInstance(taskInstance.getContext());
		initContextForInstanceEdit(taskInstance);
		event.setNavigation(NavigationConstants.NAVIGATE_TASK_DETAILS_PAGE);
	}

	/**
	 * Save task.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the string
	 */
	public String saveTask(TaskInstance taskInstance) {
		workflowService.updateTaskInstance(taskInstance);
		reloadCaseInstance();
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Cancel task editing.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return the string
	 */
	public String cancelTaskEdit(AbstractTaskInstance taskInstance) {
		return NavigationConstants.BACKWARD;
	}

	/**
	 * Reassign operation observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void reassignOperationObserver(
			@Observes @EMFAction(value = ActionTypeConstants.REASSIGN_TASK, target = TaskInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.reassignOperationObserver");
		getDocumentContext().put(BaseTaskActions.SELECTED_TASK_INSTANCE, event.getInstance());
		event.setNavigation(NavigationConstants.RELOAD_PAGE);
	}

	/**
	 * Hold task action observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void holdTask(
			@Observes @EMFAction(value = ActionTypeConstants.SUSPEND, target = TaskInstance.class) final EMFActionEvent event) {
		TaskInstance taskInstance = (TaskInstance) event.getInstance();
		if (taskInstance != null) {
			log.debug("Executing observer WorkflowTaskAction.holdTask [" + taskInstance.getId()
					+ "]");
			eventService.fire(new TaskOnHoldEvent(taskInstance));
			workflowService.updateTaskInstance(taskInstance);
			reloadCaseInstance();
			reloadTaskInstance(taskInstance);
			event.setInstance(taskInstance);
			event.setNavigation(NavigationConstants.RELOAD_PAGE);
		} else {
			log.error("Cann't hold task because there is no task in context!");
			return;
		}
	}

	/**
	 * (Immediate operation observer) Hold task action observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void holdTaskImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.SUSPEND, target = TaskInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.holdTaskImmediate");
		event.setHandled(true);
	}

	/**
	 * Activate task action observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void activateTask(
			@Observes @EMFAction(value = ActionTypeConstants.RESTART, target = TaskInstance.class) final EMFActionEvent event) {
		AbstractTaskInstance taskInstance = (AbstractTaskInstance) event.getInstance();
		if (taskInstance != null) {
			log.debug("Executing observer WorkflowTaskAction.activateTask task["
					+ taskInstance.getId() + "]");

			if (taskInstance instanceof TaskInstance) {
				eventService.fire(new TaskActivateEvent((TaskInstance) taskInstance));
				workflowService.updateTaskInstance((TaskInstance) taskInstance);
				// TODO reload case instance?
				reloadCaseInstance();
			} else if (taskInstance instanceof StandaloneTaskInstance) {
				eventService.fire(new StandaloneTaskActivateEvent(
						(StandaloneTaskInstance) taskInstance));
				taskService.save(taskInstance, null);
			}

			reloadTaskInstance(taskInstance);
			event.setInstance(taskInstance);
			event.setNavigation(NavigationConstants.RELOAD_PAGE);
		} else {
			log.error("Cann't restart task because there is no task in context!");
			return;
		}
	}

	/**
	 * (Immediate operation observer) Activate task action observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void activateTaskImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.RESTART, target = TaskInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.activateTaskImmediate");
		event.setHandled(true);
	}

	/**
	 * Claim task from pool action handler. TODO: Optimization
	 * 
	 * @param event
	 *            the event
	 */
	public void claimTask(
			@Observes @EMFAction(value = ActionTypeConstants.CLAIM, target = TaskInstance.class) final EMFActionEvent event) {
		Instance instance = event.getInstance();
		TaskInstance taskInstance = null;
		if (instance != null) {
			taskInstance = (TaskInstance) instance;
			log.debug("Executing observer WorkflowTaskAction.claimTask task["
					+ taskInstance.getId() + "]");

			eventService.fire(new TaskClaimEvent(taskInstance));
			workflowService.updateTaskInstance(taskInstance);

			reloadTaskInstance(taskInstance);
			event.setInstance(taskInstance);
			getDocumentContext().put(DocumentContext.FORCE_RELOAD_FORM, Boolean.TRUE);
			event.setNavigation(NavigationConstants.RELOAD_PAGE);
			getDocumentContext().setFormMode(FormViewMode.EDIT);
			getDocumentContext().clearSelectedAction();
		} else {
			log.error("Cann't claim task because there is no task in context!");
			return;
		}
	}

	/**
	 * (Immediate operation observer) Claim task from pool action handler.
	 * 
	 * @param event
	 *            the event
	 */
	public void claimTaskImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.CLAIM, target = TaskInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.claimTaskImmediate");
		event.setHandled(true);
	}

	/**
	 * Release task to pool action handler.
	 * 
	 * @param event
	 *            the event
	 */
	public void releaseTask(
			@Observes @EMFAction(value = ActionTypeConstants.RELEASE, target = TaskInstance.class) final EMFActionEvent event) {
		Instance instance = event.getInstance();
		TaskInstance taskInstance = null;
		if (instance != null) {
			taskInstance = (TaskInstance) instance;
			log.debug("Executing observer WorkflowTaskAction.releaseTask task["
					+ taskInstance.getId() + "]");

			eventService.fire(new TaskReleaseEvent(taskInstance));
			workflowService.updateTaskInstance(taskInstance);

			reloadTaskInstance(taskInstance);
			event.setInstance(taskInstance);
			getDocumentContext().put(DocumentContext.FORCE_RELOAD_FORM, Boolean.TRUE);
			event.setNavigation(NavigationConstants.RELOAD_PAGE);
			getDocumentContext().setFormMode(FormViewMode.PREVIEW);
		} else {
			log.error("Cann't release task because there is no task in context!");
			return;
		}
	}

	/**
	 * (Immediate operation observer) Release task to pool action handler.
	 * 
	 * @param event
	 *            the event
	 */
	public void releaseTaskImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.RELEASE, target = TaskInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer WorkflowTaskAction.releaseTaskImmediate");
		event.setHandled(true);
	}

	/**
	 * Reload task instance.
	 * 
	 * @param taskInstance
	 *            the task instance
	 */
	private void reloadTaskInstance(AbstractTaskInstance taskInstance) {
		instanceService.refresh(taskInstance);
	}

	/**
	 * Getter method for items.
	 * 
	 * @return the items
	 */
	public String getItems() {
		return items;
	}

	/**
	 * Setter method for items.
	 * 
	 * @param items
	 *            the items to set
	 */
	public void setItems(String items) {
		this.items = items;
	}

	/**
	 * Getter method for usersList.
	 * 
	 * @return the usersList
	 */
	public List<SelectorItem> getUsersList() {
		return usersList;
	}

	/**
	 * Setter method for usersList.
	 * 
	 * @param users
	 *            the usersList to set
	 */
	public void setUsersList(List<SelectorItem> users) {
		this.usersList = users;
	}

	/**
	 * Getter method for newAssigneeId.
	 * 
	 * @return the newAssigneeId
	 */
	public String getNewAssigneeId() {
		return newAssigneeId;
	}

	/**
	 * Setter method for newAssigneeId.
	 * 
	 * @param assigneeId
	 *            the newAssigneeId to set
	 */
	public void setNewAssigneeId(String assigneeId) {
		this.newAssigneeId = assigneeId;
	}

	/**
	 * Getter method for picKey.
	 * 
	 * @return the picKey
	 */
	public String getPicKey() {
		return picKey;
	}

	/**
	 * Setter method for picKey.
	 * 
	 * @param picKey
	 *            the picKey to set
	 */
	public void setPicKey(String picKey) {
		this.picKey = picKey;
	}

	/**
	 * Getter method for hasEditPermission.
	 * 
	 * @return the hasEditPermission
	 */
	public boolean isHasEditPermission() {
		return hasEditPermission;
	}

}
