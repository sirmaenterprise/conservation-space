package com.sirma.itt.pm.schedule.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.json.JSONObject;

import com.sirma.itt.emf.domain.JsonRepresentable;
import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.util.JsonUtil;

// TODO: Auto-generated Javadoc
/**
 * Entity class that represents a single dependency between two tasks.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "pmfs_scheduledependency")
@org.hibernate.annotations.Table(appliesTo = "pmfs_scheduledependency", indexes = {
		@Index(name = "idx_sdep_from", columnNames = { "from_id" }),
		@Index(name = "idx_sdep_to", columnNames = { "to_id" }),
		@Index(name = "idx_sdep_ft", columnNames = { "from_id", "to_id" }),
		@Index(name = "idx_sdep_schid", columnNames = { "schedule_id" }) })
public class ScheduleDependency extends BaseEntity implements JsonRepresentable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 9001492228302360103L;

	/** The type of dependency start to start. */
	public static final int TYPE_START_TO_START = 0;
	/** The type of dependency start to end. */
	public static final int TYPE_START_TO_END = 1;
	/** The type of dependency end to start. */
	public static final int TYPE_END_TO_START = 2;
	/** The type of dependency end to end. */
	public static final int TYPE_END_TO_END = 3;

	/** The from. */
	@Column(name = "from_id", nullable = false)
	private Long from;

	/** The to. */
	@Column(name = "to_id", nullable = false)
	private Long to;

	/** The type. */
	@Column(name = "type", nullable = false)
	private Integer type;

	/** The lag. */
	@Column(name = "lag", nullable = false)
	private Integer lag = 0;

	/** The css class. */
	@Column(name = "css_class", length = 50, nullable = true)
	private String cssClass = "";

	/** The lag unit. */
	@Column(name = "lagunit", length = 10, nullable = false)
	private String lagUnit = "d";

	/** The schedule id. */
	@Column(name = "schedule_id", nullable = false)
	private Long scheduleId;

	/**
	 * Gets the from.
	 * 
	 * @return the from
	 */
	public Long getFrom() {
		return from;
	}

	/**
	 * Sets the from.
	 * 
	 * @param from
	 *            the new from
	 */
	public void setFrom(Long from) {
		this.from = from;
	}

	/**
	 * Gets the to.
	 * 
	 * @return the to
	 */
	public Long getTo() {
		return to;
	}

	/**
	 * Sets the to.
	 * 
	 * @param to
	 *            the new to
	 */
	public void setTo(Long to) {
		this.to = to;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * Gets the lag.
	 * 
	 * @return the lag
	 */
	public Integer getLag() {
		return lag;
	}

	/**
	 * Sets the lag.
	 * 
	 * @param lag
	 *            the new lag
	 */
	public void setLag(Integer lag) {
		this.lag = lag;
	}

	/**
	 * Gets the lag unit.
	 * 
	 * @return the lag unit
	 */
	public String getLagUnit() {
		return lagUnit;
	}

	/**
	 * Sets the lag unit.
	 * 
	 * @param lagUnit
	 *            the new lag unit
	 */
	public void setLagUnit(String lagUnit) {
		this.lagUnit = lagUnit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ScheduleDependency [from=" + from + ", to=" + to + ", type=" + type + ", lag="
				+ lag + ", cssClass=" + cssClass + ", lagUnit=" + lagUnit + ", scheduleId="
				+ scheduleId + "]";
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
	 * @param cssClass the cssClass to set
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
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
	 * @param scheduleId the scheduleId to set
	 */
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "Id", getId());
		JsonUtil.addToJson(jsonObject, "From", getFrom());
		JsonUtil.addToJson(jsonObject, "To", getTo());
		JsonUtil.addToJson(jsonObject, "Type", getType());
		JsonUtil.addToJson(jsonObject, "Lag", getLag());
		JsonUtil.addToJson(jsonObject, "Cls", getCssClass());
		JsonUtil.addToJson(jsonObject, "LagUnit", getLagUnit());
		return jsonObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		setId(JsonUtil.getLongValue(jsonObject, "Id"));
		setFrom(JsonUtil.getLongValue(jsonObject, "From"));
		setTo(JsonUtil.getLongValue(jsonObject, "To"));
		setType(JsonUtil.getIntegerValue(jsonObject, "Type"));
		setLag(JsonUtil.getIntegerValue(jsonObject, "Lag"));
		setCssClass(JsonUtil.getStringValue(jsonObject, "Cls"));
		setLagUnit(JsonUtil.getStringValue(jsonObject, "LagUnit"));
	}

}
