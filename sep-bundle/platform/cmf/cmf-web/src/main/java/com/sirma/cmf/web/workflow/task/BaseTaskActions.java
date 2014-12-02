package com.sirma.cmf.web.workflow.task;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskReassignEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskReassignEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * This class will help for merging operations based on the workflow or standalone tasks. The key
 * concept is treat the task as abstract task {@see AbstractTaskInstance}.
 * 
 * @author cdimitrov
 */
@Named
@ViewAccessScoped
public class BaseTaskActions extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1600465033376699669L;

	/** The assignee id used from reassign operation. */
	private String newAssigneeId;

	/** The selected task constant. */
	public static final String SELECTED_TASK_INSTANCE = "selectedTaskInstance";

	/** The standalone task reassign event service. */
	@Inject
	private Event<StandaloneTaskReassignEvent> standaloneTaskReassignEvent;

	/** The workflow task reassign event service. */
	@Inject
	private Event<TaskReassignEvent> taskReassignEvent;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The workflow task service. */
	@Inject
	private WorkflowService workflowService;

	/**
	 * Reassign task to the specific user.
	 * 
	 * @param task
	 *            current abstract task
	 * @return navigation string
	 */
	public String reassignTask(AbstractTaskInstance task) {

		String navigation = NavigationConstants.RELOAD_PAGE;

		if (StringUtils.isNotNullOrEmpty(newAssigneeId)) {

			Map<String, Serializable> properties = task.getProperties();
			String oldOwner = (String) properties.get(TaskProperties.TASK_OWNER);

			properties.put(TaskProperties.TASK_OWNER, newAssigneeId);
			properties.put(TaskProperties.TASK_ASSIGNEE, newAssigneeId);

			if (task instanceof StandaloneTaskInstance) {

				standaloneTaskReassignEvent.fire(new StandaloneTaskReassignEvent(
						(StandaloneTaskInstance) task, oldOwner));

				taskService.save(
						task,
						new Operation(getDocumentContext().getCurrentOperation(
								StandaloneTaskInstance.class.getSimpleName())));

				getDocumentContext().put(DocumentContext.FORCE_RELOAD_FORM, Boolean.TRUE);

			} else if (task instanceof TaskInstance) {

				TaskInstance taskInstance = (TaskInstance) task;

				taskReassignEvent.fire(new TaskReassignEvent(taskInstance, oldOwner));

				workflowService.updateTaskInstance(taskInstance);

				reloadCaseInstance();

				contextReinitialize(taskInstance);
				
				navigation = NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
			}
		}

		return navigation;
	}

	/**
	 * Initialize the context for case instance.
	 * 
	 * @param instance
	 *            current workflow task
	 */
	private void contextReinitialize(TaskInstance instance) {

		WorkflowInstanceContext workFlowContext = (WorkflowInstanceContext) instance
				.getOwningInstance();

		Instance owningInstance = workFlowContext.getOwningInstance();

		if (owningInstance != null) {
			getDocumentContext().setCurrentInstance(owningInstance);
			Instance rootInstance = InstanceUtil.getRootInstance(owningInstance, true);
			if(rootInstance != null){
				getDocumentContext().setRootInstance(rootInstance);
			}
		}
	}

	/**
	 * Getter method for retrieving the new assignee id.
	 * 
	 * @return new assignee id
	 */
	public String getNewAssigneeId() {
		return newAssigneeId;
	}

	/**
	 * Setter method for storing assignee id.
	 * 
	 * @param assigneeId
	 *            new assignee id
	 */
	public void setNewAssigneeId(String assigneeId) {
		this.newAssigneeId = assigneeId;
	}

}
