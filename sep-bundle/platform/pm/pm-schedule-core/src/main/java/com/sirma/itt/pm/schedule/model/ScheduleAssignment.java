package com.sirma.itt.pm.schedule.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.json.JSONObject;

import com.sirma.itt.emf.domain.JsonRepresentable;
import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Object that represents an assignment of a resource to a task from a concrete schedule
 * 
 * @author BBonev
 */
@Entity
@Table(name = "pmfs_scheduleassignment")
@org.hibernate.annotations.Table(appliesTo = "pmfs_scheduleassignment", indexes = {
		@Index(name = "idx_sa_sid", columnNames = "schedule_id"),
		@Index(name = "idx_sa_sidr", columnNames = { "schedule_id", "resource_id" }),
		@Index(name = "idx_sa_rid", columnNames = "resource_id") })
public class ScheduleAssignment extends BaseEntity implements JsonRepresentable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4575543522212521795L;

	/** The task id. */
	@Column(name = "task_id", nullable = false)
	private Long taskId;

	/** The resource id. */
	@Column(name = "resource_id", length = 100, nullable = false)
	private String resourceId;

	/** The units. */
	@Column(name = "units", nullable = false)
	private Integer units = 0;

	/** The project id. */
	@Column(name = "schedule_id", nullable = false)
	private Long scheduleId;

	/**
	 * Getter method for taskId.
	 * 
	 * @return the taskId
	 */
	public Long getTaskId() {
		return taskId;
	}

	/**
	 * Setter method for taskId.
	 * 
	 * @param taskId
	 *            the taskId to set
	 */
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	/**
	 * Getter method for resourceId.
	 * 
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * Setter method for resourceId.
	 * 
	 * @param resourceId
	 *            the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Getter method for units.
	 * 
	 * @return the units
	 */
	public Integer getUnits() {
		return units;
	}

	/**
	 * Setter method for units.
	 * 
	 * @param units
	 *            the units to set
	 */
	public void setUnits(Integer units) {
		this.units = units;
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

	@Override
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "Id", getId());
		JsonUtil.addToJson(jsonObject, "TaskId", getTaskId());
		JsonUtil.addToJson(jsonObject, "ResourceId", getResourceId());
		JsonUtil.addToJson(jsonObject, "Units", getUnits());
		return jsonObject;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		setId(JsonUtil.getLongValue(jsonObject, "Id"));
		setTaskId(JsonUtil.getLongValue(jsonObject, "TaskId"));
		setResourceId(JsonUtil.getStringValue(jsonObject, "ResourceId"));
		setUnits(JsonUtil.getIntegerValue(jsonObject, "Units"));
	}

}
