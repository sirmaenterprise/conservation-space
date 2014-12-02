package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;

import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.ScheduleSynchronizationInstance;

/**
 * Context class that represents a workflow instance. It contains all properties that are for the
 * workflow and the common task properties. It's the connection between the workflow and the owning
 * case.
 *
 * @author BBonev
 */
public class WorkflowInstanceContext extends EmfInstance implements InstanceContext,
		ScheduleSynchronizationInstance {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6538954298011444155L;

	/** The workflow instance id. */
	private String workflowInstanceId;

	/** The active. */
	private Boolean active;

	/**
	 * Checks if the current workflow is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		// FIXME REVIEW
		if (getActive() == null) {
			setActive((getProperties() != null)
					&& "WFST01".equals(getProperties().get(WorkflowProperties.STATUS)));
		}
		return getActive().booleanValue();
	}

	/**
	 * Getter method for workflowInstanceId.
	 *
	 * @return the workflowInstanceId
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkflowInstanceContext [id=");
		builder.append(getId());
		builder.append(", workflowId=");
		builder.append(getIdentifier());
		builder.append(", revision=");
		builder.append(getRevision());
		builder.append(", workflowInstanceId=");
		builder.append(workflowInstanceId);
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append(", owningReference=");
		builder.append(getOwningReference());
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Setter method for active.
	 *
	 * @param active
	 *            the active to set
	 */
	//REVIEW setting active is not consistent
	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public boolean hasChildren() {
		for (Serializable serializable : getProperties().values()) {
			if (serializable instanceof Node) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Node getChild(String name) {
		Serializable serializable = getProperties().get(name);
		if (serializable instanceof Node) {
			return (Node) serializable;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((active == null) ? 0 : active.hashCode());
		result = (prime * result)
				+ ((workflowInstanceId == null) ? 0 : workflowInstanceId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WorkflowInstanceContext other = (WorkflowInstanceContext) obj;
		if (active == null) {
			if (other.active != null) {
				return false;
			}
		} else if (!active.equals(other.active)) {
			return false;
		}
		if (workflowInstanceId == null) {
			if (other.workflowInstanceId != null) {
				return false;
			}
		} else if (!workflowInstanceId.equals(other.workflowInstanceId)) {
			return false;
		}
		return true;
	}

}
