package com.sirma.itt.pm.schedule.model;

import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity used to persist {@link com.sirma.itt.pm.schedule.model.ScheduleEntry}
 *
 * @author BBonev
 */
@Entity
@Table(name = "pmfs_scheduleentryentity")
@org.hibernate.annotations.Table(appliesTo = "pmfs_scheduleentryentity", indexes = {
		@Index(name = "idx_pm_scheen_sid", columnNames = "schedule_id"),
		@Index(name = "idx_pms_scheen_aidt", columnNames = { "actualinstanceid",
				"actualinstancetype" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "actualInstance.sourceType", joinColumns = @JoinColumn(name = "actualinstancetype", nullable = true)) })
public class ScheduleEntryEntity extends BaseEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -486507718934863821L;
	/** The identifier. */
	@Column(name = "actualinstancedefinition", length = 100, nullable = true)
	private String actualInstanceDefinition;
	/** The link to the actual instance. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "actualinstanceid", length = 50, nullable = true)) })
	private LinkSourceId actualInstance;
	/** The content management id. Unique identifier for the particular instance based on the project id. */
	@Column(name = "cmid", length = 100, nullable = true)
	private String contentManagementId;
	/** The project database id. */
	@Column(name = "schedule_id", nullable = false)
	private Long scheduleId;
	/**
	 * The parent prototype instance Id. If <code>null</code> then this is the project prototype
	 * representation
	 */
	@Column(name = "parent_id", nullable = true)
	private Long parentId;
	/** The leaf. */
	@Column(name = "leaf", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean leaf = Boolean.TRUE;
	/** The css class. */
	@Column(name = "cssclass", length = 100, nullable = true)
	private String cssClass;
	/** The actual instance id. */
	@Column(name = "actualinstancedbid", length = 100, nullable = true)
	private String actualInstanceId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "startdate", nullable = true)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "enddate", nullable = true)
	private Date endDate;
	@Column(name = "status", nullable = true)
	private String status;
	/**
	 * Getter method for projectDefinitionId.
	 *
	 * @return the projectDefinitionId
	 */
	public String getActualInstanceDefinition() {
		return actualInstanceDefinition;
	}

	/**
	 * Setter method for projectDefinitionId.
	 *
	 * @param projectDefinitionId
	 *            the projectDefinitionId to set
	 */
	public void setActualInstanceDefinition(String projectDefinitionId) {
		actualInstanceDefinition = projectDefinitionId;
	}

	/**
	 * Getter method for actualInstance.
	 *
	 * @return the actualInstance
	 */
	public LinkSourceId getActualInstance() {
		return actualInstance;
	}

	/**
	 * Setter method for actualInstance.
	 *
	 * @param actualInstance
	 *            the actualInstance to set
	 */
	public void setActualInstance(LinkSourceId actualInstance) {
		this.actualInstance = actualInstance;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduleEntryEntity [id=");
		builder.append(getId());
		builder.append(", scheduleId=");
		builder.append(scheduleId);
		builder.append(", projectDefinitionId=");
		builder.append(actualInstanceDefinition);
		builder.append(", contentManagementId=");
		builder.append(contentManagementId);
		builder.append(", parentId=");
		builder.append(parentId);
		builder.append(", actualInstance=");
		builder.append(actualInstance);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for scheduleId.
	 *
	 * @return the scheduleId
	 */
	public Long getScheduleId() {
		return scheduleId;
	}

	/**
	 * Setter method for scheduleId.
	 *
	 * @param scheduleId
	 *            the scheduleId to set
	 */
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	/**
	 * Getter method for leaf.
	 *
	 * @return the leaf
	 */
	public Boolean getLeaf() {
		return leaf;
	}

	/**
	 * Setter method for leaf.
	 *
	 * @param leaf
	 *            the leaf to set
	 */
	public void setLeaf(Boolean leaf) {
		this.leaf = leaf;
	}

	/**
	 * Getter method for cssClass.
	 *
	 * @return the cssClass
	 */
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * Setter method for cssClass.
	 *
	 * @param cssClass
	 *            the cssClass to set
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	/**
	 * Getter method for actualInstanceId.
	 *
	 * @return the actualInstanceId
	 */
	public String getActualInstanceId() {
		return actualInstanceId;
	}

	/**
	 * Setter method for actualInstanceId.
	 *
	 * @param actualInstanceId the actualInstanceId to set
	 */
	public void setActualInstanceId(String actualInstanceId) {
		this.actualInstanceId = actualInstanceId;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}
