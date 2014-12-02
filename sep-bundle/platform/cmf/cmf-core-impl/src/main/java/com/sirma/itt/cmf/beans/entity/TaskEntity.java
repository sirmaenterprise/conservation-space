package com.sirma.itt.cmf.beans.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Index;

import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Task entity
 *
 * @author BBonev
 */
@Entity
@Table(name = "cmf_taskEntity")
@org.hibernate.annotations.Table(appliesTo = "cmf_taskEntity", indexes = {
		@Index(name = "idx_tske_dmId", columnNames = "dmId"),
		@Index(name = "idx_tske_cmId", columnNames = "cmId"),
		@Index(name = "idx_tske_def", columnNames = "definitionId"),
		@Index(name = "idx_tske_def_rev", columnNames = { "definitionId", "revision" }),
		@Index(name = "idx_tske_ownref", columnNames = { "owninginstanceid", "owninginstancetype" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "owningInstance.sourceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)) })
public class TaskEntity extends BaseStringIdEntity implements BidirectionalMapping, PathElement,
		VersionableEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6646687667027556290L;

	/** The document management id. */
	@Column(name = "dmId", length = 100, nullable = true)
	private String documentManagementId;

	/** The content management id. */
	@Column(name = "cmId", length = 100, nullable = true)
	private String contentManagementId;

	/** The case definition id. */
	@Column(name = "definitionId", length = 100, nullable = false)
	private String definitionId;

	/** The case revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;

	/**
	 * Versionable field for optimistic locking <br/>
	 */
	@Column(name = "version", nullable = true)
	@Version
	private Long version;

	/** The workflow instance id. */
	@Column(name = "workflowInstanceId", length = 100, nullable = true)
	private String workflowInstanceId;

	/** The state. */
	@Column(name = "state", nullable = true)
	private TaskState state;

	/** The parent path. */
	@Column(name = "parentPath", length = 200, nullable = true)
	private String parentPath;

	/** The document management id. */
	@Column(name = "dmsId", length = 100, nullable = true)
	private String dmsId;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owninginstanceid", length = 50, nullable = true)) })
	private LinkSourceId owningInstance;

	@Column(name = "taskType")
	private TaskType taskType;
	/** The path. */
	@Column(name = "treePath", length = 512, nullable = true)
	private String treePath;
	/**
	 * Getter method for documentManagementId.
	 *
	 * @return the documentManagementId
	 */
	public String getDocumentManagementId() {
		return documentManagementId;
	}

	/**
	 * Setter method for documentManagementId.
	 *
	 * @param documentManagementId
	 *            the documentManagementId to set
	 */
	public void setDocumentManagementId(String documentManagementId) {
		this.documentManagementId = documentManagementId;
	}

	/**
	 * Getter method for contentManagementId.
	 *
	 * @return the contentManagementId
	 */
	public String getContentManagementId() {
		return contentManagementId;
	}

	/**
	 * Setter method for contentManagementId.
	 *
	 * @param contentManagementId
	 *            the contentManagementId to set
	 */
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}

	/**
	 * Getter method for caseDefinitionId.
	 *
	 * @return the caseDefinitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter method for caseDefinitionId.
	 *
	 * @param caseDefinitionId
	 *            the caseDefinitionId to set
	 */
	public void setDefinitionId(String caseDefinitionId) {
		definitionId = caseDefinitionId;
	}

	/**
	 * Getter method for caseRevision.
	 *
	 * @return the caseRevision
	 */
	public Long getRevision() {
		return revision;
	}

	/**
	 * Setter method for caseRevision.
	 *
	 * @param caseRevision
	 *            the caseRevision to set
	 */
	public void setRevision(Long caseRevision) {
		revision = caseRevision;
	}

	/**
	 * Getter method for version.
	 *
	 * @return the version
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	/**
	 * Setter method for version.
	 *
	 * @param version
	 *            the version to set
	 */
	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * Getter method for container.
	 *
	 * @return the container
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * Setter method for container.
	 *
	 * @param container
	 *            the container to set
	 */
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((definitionId == null) ? 0 : definitionId.hashCode());
		result = (prime * result) + ((container == null) ? 0 : container.hashCode());
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
		if (!(obj instanceof TaskEntity)) {
			return false;
		}
		TaskEntity other = (TaskEntity) obj;
		if (definitionId == null) {
			if (other.definitionId != null) {
				return false;
			}
		} else if (!definitionId.equals(other.definitionId)) {
			return false;
		}
		if (container == null) {
			if (other.container != null) {
				return false;
			}
		} else if (!container.equals(other.container)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskEntity [id=");
		builder.append(getId());
		builder.append(", definitionId=");
		builder.append(definitionId);
		builder.append(", taskType=");
		builder.append(taskType);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", taskInstanceId=");
		builder.append(documentManagementId);
		builder.append(", contentManagementId=");
		builder.append(contentManagementId);
		builder.append(", workflowInstanceId=");
		builder.append(workflowInstanceId);
		builder.append(", state=");
		builder.append(state);
		builder.append(", container=");
		builder.append(container);
		builder.append(", treePath=");
		builder.append(treePath);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getDefinitionId();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setDefinitionId(identifier);
	}

	@Override
	public void initBidirection() {
		// nothing to do here for now
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
	 * @param workflowInstanceId the workflowInstanceId to set
	 */
	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}

	/**
	 * Getter method for state.
	 *
	 * @return the state
	 */
	public TaskState getState() {
		return state;
	}

	/**
	 * Setter method for state.
	 *
	 * @param state the state to set
	 */
	public void setState(TaskState state) {
		this.state = state;
	}

	/**
	 * Getter method for parentPath.
	 *
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Setter method for parentPath.
	 *
	 * @param parentPath the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
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
	 * @param owningInstance the owningInstance to set
	 */
	public void setOwningInstance(LinkSourceId owningInstance) {
		this.owningInstance = owningInstance;
	}

	/**
	 * @return the dmsId
	 */
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * @param dmsId the dmsId to set
	 */
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
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
	 * @param taskType the taskType to set
	 */
	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
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
	 * @param treePath the treePath to set
	 */
	public void setTreePath(String treePath) {
		this.treePath = treePath;
	}
}
