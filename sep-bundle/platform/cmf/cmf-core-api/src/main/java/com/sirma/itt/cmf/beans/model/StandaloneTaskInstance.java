package com.sirma.itt.cmf.beans.model;

import java.util.Date;

import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.date.DateUtils.DatePart;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Instance implementation for task instance that is started as a standalone task and that is not a
 * part of any workflow.
 * 
 * @author BBonev
 */
public class StandaloneTaskInstance extends AbstractTaskInstance {

	private static final long serialVersionUID = 3819385631306084045L;

	/** id of related object that task is associated with. */
	private String parentContextId;

	/** type for dms task for standalone tasks. */
	private String dmsTaskType;

	/** The path of all standalone tasks that form a branch of a tree. */
	private String treePath;

	@Override
	public boolean isStandalone() {
		return true;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	/**
	 * @return the parentContextId
	 */
	public String getParentContextId() {
		return parentContextId;
	}

	/**
	 * @param parentContextId
	 *            the parentContextId to set
	 */
	public void setParentContextId(String parentContextId) {
		this.parentContextId = parentContextId;
	}

	/**
	 * @return the dmsTaskType
	 */
	public String getDmsTaskType() {
		return dmsTaskType;
	}

	/**
	 * @param dmsTaskType
	 *            the dmsTaskType to set
	 */
	public void setDmsTaskType(String dmsTaskType) {
		this.dmsTaskType = dmsTaskType;
	}

	@Override
	public boolean isOverDue() {
		if (getProperties() != null) {
			Date plannedEndTime = (Date) getProperties().get(DefaultProperties.PLANNED_END_DATE);
			if (plannedEndTime != null) {
				if (DateUtils.isBefore(DatePart.SECOND, plannedEndTime, new Date())) {
					return true;
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StandaloneTaskInstance [id=");
		builder.append(getId());
		builder.append(", parentContextId=");
		builder.append(parentContextId);
		builder.append(", dmsTaskType=");
		builder.append(dmsTaskType);
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append(", taskInstanceId=");
		builder.append(taskInstanceId);
		builder.append(", taskDefinitionId=");
		builder.append(getIdentifier());
		builder.append(", state=");
		builder.append(state);
		builder.append(", revision=");
		builder.append(getRevision());
		builder.append(", editable=");
		builder.append(editable);
		builder.append(", reassignable=");
		builder.append(reassignable);
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", owningReference=");
		builder.append(getOwningReference());
		builder.append(", dmsId=");
		builder.append(getDmsId());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public TaskType getTaskType() {
		return TaskType.STANDALONE_TASK;
	}

	/**
	 * Getter method for treePath.
	 * 
	 * @return the treePath
	 */
	public String getTreePath() {
		return treePath;
	}

	/**
	 * Setter method for treePath.
	 * 
	 * @param treePath
	 *            the treePath to set
	 */
	public void setTreePath(String treePath) {
		this.treePath = treePath;
	}
}
