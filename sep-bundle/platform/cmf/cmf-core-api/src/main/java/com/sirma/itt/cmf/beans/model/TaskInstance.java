package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map.Entry;

import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.date.DateUtils.DatePart;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.PathElementProxy;

/**
 * The Class TaskInstance is transient wrapper for tasks.
 *
 * @author BBanchev
 * @author BBonev
 */
public class TaskInstance extends AbstractTaskInstance {

	private static final long serialVersionUID = -11541304441677446L;

	private String workflowInstanceId;

	private String workflowDefinitionId;

	private PathElement taskInstancePath;

	protected WorkflowInstanceContext context;

	/**
	 * The parent task. This will not be stored, the field is used as a temporary value for specific
	 * operations.
	 */
	private transient TaskInstance parentTask;

	/**
	 * Gets the workflow instance id.
	 *
	 * @return the workflow instance id
	 */
	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	/**
	 * Setter method for workflowInstanceId.
	 *
	 * @param workflowInstanceId
	 *            the workflowInstanceId to set
	 */
	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}

	/**
	 * Getter method for workflowDefinitionId.
	 *
	 * @return the workflowDefinitionId
	 */
	public String getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	/**
	 * Setter method for workflowDefinitionId.
	 *
	 * @param workflowDefinitionId
	 *            the workflowDefinitionId to set
	 */
	public void setWorkflowDefinitionId(String workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskInstance [id=");
		builder.append(getId());
		builder.append(", workflowDefinitionId=");
		builder.append(workflowDefinitionId);
		builder.append(", revision=");
		builder.append(getRevision());
		builder.append(", workflowInstanceId=");
		builder.append(workflowInstanceId);
		builder.append(", taskInstanceId=");
		builder.append(taskInstanceId);
		builder.append(", taskDefinitionId=");
		builder.append(getIdentifier());
		builder.append(", state=");
		builder.append(state);
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append(", context=");
		builder.append(context);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public PathElement getParentElement() {
		return getContext();
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * Setter method for context.
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(WorkflowInstanceContext context) {
		if ((context != null) && (context.getProperties() != null) && (getProperties() != null)) {
			for (Entry<String, Serializable> entry : context.getProperties().entrySet()) {
				Serializable serializable = getProperties().get(entry.getKey());
				if ((serializable == null) && (entry.getValue() != null)) {
					getProperties().put(entry.getKey(), entry.getValue());
				}
			}
		}
		this.context = context;
	}

	/**
	 * Gets the task path element.
	 *
	 * @return the task path element
	 */
	public PathElement getTaskPathElement() {
		if (taskInstancePath == null) {
			taskInstancePath = new PathElementProxy(getWorkflowDefinitionId() + "/"
					+ getIdentifier(), null, this);
		}
		return taskInstancePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((workflowInstanceId == null) ? 0 : workflowInstanceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof TaskInstance)) {
			return false;
		}
		TaskInstance other = (TaskInstance) obj;
		if (workflowInstanceId == null) {
			if (other.workflowInstanceId != null) {
				return false;
			}
		} else if (!workflowInstanceId.equals(other.workflowInstanceId)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for context.
	 *
	 * @return the context
	 */
	public WorkflowInstanceContext getContext() {
		return context;
	}

	/**
	 * Checks if this task instance if over due.
	 *
	 * @return true, if is over due
	 */
	@Override
	public boolean isOverDue() {
		// TODO - how to load the context when instance is partially created from solr
		// CMF-1626 get the due date from task
		Date plannedEndTime = (Date) getProperties().get(WorkflowProperties.PLANNED_END_DATE);
		if (plannedEndTime != null) {
			if (DateUtils.isBefore(DatePart.SECOND, plannedEndTime, new Date())) {
				return true;
			}
			return false;
		}
		if (getContext() != null) {
			plannedEndTime = (Date) getContext().getProperties().get(
					WorkflowProperties.PLANNED_END_DATE);
			if ((plannedEndTime != null)
					&& DateUtils.isBefore(DatePart.SECOND, plannedEndTime, new Date())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isStandalone() {
		return false;
	}

	/**
	 * Getter method for parentTask.
	 *
	 * @return the parentTask
	 */
	public TaskInstance getParentTask() {
		return parentTask;
	}

	/**
	 * Setter method for parentTask.
	 *
	 * @param parentTask
	 *            the parentTask to set
	 */
	public void setParentTask(TaskInstance parentTask) {
		this.parentTask = parentTask;
	}

	@Override
	public TaskType getTaskType() {
		return TaskType.WORKFLOW_TASK;
	}

}
