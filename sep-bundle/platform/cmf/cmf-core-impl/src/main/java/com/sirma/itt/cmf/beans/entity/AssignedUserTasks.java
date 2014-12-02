package com.sirma.itt.cmf.beans.entity;

import java.util.Set;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity that will hold information about tasks assigned to user from each workflow and for
 * particular instance
 *
 * @author BBonev
 */
@Entity
@Table(name = "cmf_assignedUserTasks")
@org.hibernate.annotations.Table(appliesTo = "cmf_assignedUserTasks", indexes = {
		@Index(name = "idx_aut_user_ref_nu_a", columnNames = { "userid", "owninginstanceid",
				"owninginstancetype", "active" }),
		@Index(name = "idx_aut_contextid", columnNames = { "contextreferenceid",
				"contextreferencetype" }),
		@Index(name = "idx_aut_ownref", columnNames = { "owninginstanceid", "owninginstancetype" }) })
@AssociationOverrides(value = {
		@AssociationOverride(name = "owningInstance.sourceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)),
		@AssociationOverride(name = "contextReference.sourceType", joinColumns = @JoinColumn(name = "contextreferencetype", nullable = true)) })
public class AssignedUserTasks extends BaseEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3429987972078621572L;
	/** The user id. */
	@Column(name = "userId", length = 50, nullable = false)
	private String userId;
	/** The task instance id. */
	@Column(name = "taskInstanceId", length = 100, nullable = false)
	private String taskInstanceId;
	/** The context reference. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "contextreferenceid", length = 50, nullable = true)) })
	private LinkSourceId contextReference;
	/** The active. */
	@Column(name = "active", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean active = Boolean.TRUE;
	/** The owning instance. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owninginstanceid", length = 50, nullable = true)) })
	private LinkSourceId owningInstance;
	/** The task type. */
	@Column(name = "tasktype")
	private TaskType taskType;
	/** The pool users. */
	@Column(name = "poolUsers", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.StringSetCustomType")
	private Set<String> poolUsers;
	/** The pool groups. */
	@Column(name = "poolGroups", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.StringSetCustomType")
	private Set<String> poolGroups;

	/**
	 * Getter method for userId.
	 *
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Setter method for userId.
	 *
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Getter method for taskInstanceId.
	 *
	 * @return the taskInstanceId
	 */
	public String getTaskInstanceId() {
		return taskInstanceId;
	}

	/**
	 * Setter method for taskInstanceId.
	 *
	 * @param taskInstanceId
	 *            the taskInstanceId to set
	 */
	public void setTaskInstanceId(String taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
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
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssignedUserTasks [userId=");
		builder.append(userId);
		builder.append(", taskInstanceId=");
		builder.append(taskInstanceId);
		builder.append(", contextReference=");
		builder.append(contextReference);
		builder.append(", active=");
		builder.append(active);
		builder.append(", owningInstance=");
		builder.append(owningInstance);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for owningInstance.
	 *
	 * @return the owningInstance
	 */
	public LinkSourceId getOwningInstance() {
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 *
	 * @param owningInstance
	 *            the owningInstance to set
	 */
	public void setOwningInstance(LinkSourceId owningInstance) {
		this.owningInstance = owningInstance;
	}

	/**
	 * Getter method for contextReference.
	 *
	 * @return the contextReference
	 */
	public LinkSourceId getContextReference() {
		return contextReference;
	}

	/**
	 * Setter method for contextReference.
	 *
	 * @param contextReference
	 *            the contextReference to set
	 */
	public void setContextReference(LinkSourceId contextReference) {
		this.contextReference = contextReference;
	}

	/**
	 * Getter method for taskType.
	 *
	 * @return the taskType
	 */
	public TaskType getTaskType() {
		return taskType;
	}

	/**
	 * Setter method for taskType.
	 *
	 * @param taskType
	 *            the taskType to set
	 */
	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	/**
	 * @return the poolUsers
	 */
	public Set<String> getPoolUsers() {
		return poolUsers;
	}

	/**
	 * @param poolUsers the poolUsers to set
	 */
	public void setPoolUsers(Set<String> poolUsers) {
		this.poolUsers = poolUsers;
	}

	/**
	 * @return the poolGroups
	 */
	public Set<String> getPoolGroups() {
		return poolGroups;
	}

	/**
	 * @param poolGroups the poolGroups to set
	 */
	public void setPoolGroups(Set<String> poolGroups) {
		this.poolGroups = poolGroups;
	}

}
