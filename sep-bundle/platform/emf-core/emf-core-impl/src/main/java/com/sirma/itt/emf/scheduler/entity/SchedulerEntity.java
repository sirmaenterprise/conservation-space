package com.sirma.itt.emf.scheduler.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.SerializableValue;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;

/**
 * Represents a single scheduled entry to be executed based on time or event trigger.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_schedulerentity")
@org.hibernate.annotations.Table(appliesTo = "emf_schedulerentity", indexes = {
		@Index(name = "idx_sche_tsn", columnNames = { "nextscheduletime" }),
		@Index(name = "idx_sche_tsett", columnNames = { "eventclassid", "targetclassid",
				"targetid", "operation" }) })
public class SchedulerEntity extends BaseEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8075928904335418825L;

	/** The type of the entry. */
	@Column(name = "type", nullable = false)
	private SchedulerEntryType type;

	/** The run status of the entry. */
	@Column(name = "status", nullable = false)
	private SchedulerEntryStatus status = SchedulerEntryStatus.NOT_RUN;

	/** The next schedule time for timed and cron events. */
	@Column(name = "nextScheduleTime")
	@Temporal(TemporalType.TIMESTAMP)
	private Date nextScheduleTime;

	@Column(name = "actionClassId")
	private Integer actionClassId;

	/** The action name. */
	@Column(name = "actionName", length = 150)
	private String actionName;

	/** The number of retries the operation is tried to run. */
	@Column(name = "retries")
	private Integer retries;

	/** The event trigger. */
	@Embedded
	private EventTriggerEntity eventTrigger;

	/** The context data. */
	@JoinColumn(name = "contextdata_id")
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue contextData;
	/** The identifier. */
	@Column(name = "identifier", length = 100, unique = true, nullable = false)
	private String identifier;

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public SchedulerEntryType getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(SchedulerEntryType type) {
		this.type = type;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public SchedulerEntryStatus getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(SchedulerEntryStatus status) {
		this.status = status;
	}

	/**
	 * Getter method for nextScheduleTime.
	 *
	 * @return the nextScheduleTime
	 */
	public Date getNextScheduleTime() {
		return nextScheduleTime;
	}

	/**
	 * Setter method for nextScheduleTime.
	 *
	 * @param nextScheduleTime
	 *            the nextScheduleTime to set
	 */
	public void setNextScheduleTime(Date nextScheduleTime) {
		this.nextScheduleTime = nextScheduleTime;
	}

	/**
	 * Getter method for retries.
	 *
	 * @return the retries
	 */
	public Integer getRetries() {
		return retries;
	}

	/**
	 * Setter method for retries.
	 *
	 * @param retries
	 *            the retries to set
	 */
	public void setRetries(Integer retries) {
		this.retries = retries;
	}

	/**
	 * Getter method for contextData.
	 *
	 * @return the contextData
	 */
	public SerializableValue getContextData() {
		return contextData;
	}

	/**
	 * Setter method for contextData.
	 *
	 * @param contextData the contextData to set
	 */
	public void setContextData(SerializableValue contextData) {
		this.contextData = contextData;
	}

	/**
	 * Getter method for actionClassId.
	 *
	 * @return the actionClassId
	 */
	public Integer getActionClassId() {
		return actionClassId;
	}

	/**
	 * Setter method for actionClassId.
	 *
	 * @param actionClassId the actionClassId to set
	 */
	public void setActionClassId(Integer actionClassId) {
		this.actionClassId = actionClassId;
	}

	/**
	 * Getter method for actionName.
	 *
	 * @return the actionName
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * Setter method for actionName.
	 *
	 * @param actionName the actionName to set
	 */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchedulerEntity [id=");
		builder.append(getId());
		builder.append(", type=");
		builder.append(type);
		builder.append(", status=");
		builder.append(status);
		builder.append(", nextScheduleTime=");
		builder.append(nextScheduleTime);
		builder.append(", actionClassId=");
		builder.append(actionClassId);
		builder.append(", actionName=");
		builder.append(actionName);
		builder.append(", retries=");
		builder.append(retries);
		builder.append(", eventTrigger=");
		builder.append(getEventTrigger());
		builder.append(", contextData=");
		builder.append(contextData == null ? "NULL" : "BYTE_DATA");
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for eventTrigger.
	 *
	 * @return the eventTrigger
	 */
	public EventTriggerEntity getEventTrigger() {
		return eventTrigger;
	}

	/**
	 * Setter method for eventTrigger.
	 *
	 * @param eventTrigger the eventTrigger to set
	 */
	public void setEventTrigger(EventTriggerEntity eventTrigger) {
		this.eventTrigger = eventTrigger;
	}

	/**
	 * Getter method for identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier.
	 * 
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
