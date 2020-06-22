package com.sirma.itt.seip.tasks;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONObject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Represents a single scheduler entry.
 *
 * @author BBonev
 */
public class SchedulerEntry implements Entity<Long>, Identity, Serializable, JsonRepresentable, TenantAware {
	private static final long serialVersionUID = -788323478052588088L;

	private Long id;
	private SchedulerEntryStatus status;
	private SchedulerConfiguration configuration;
	private SchedulerContext context;
	private transient SchedulerAction action;
	private String actionName;
	private String identifier;
	private String tenantId;
	private Date expectedExecutionTime;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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
	 * Getter method for configuration.
	 *
	 * @return the configuration
	 */
	public SchedulerConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Setter method for configuration.
	 *
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(SchedulerConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Getter method for context.
	 *
	 * @return the context
	 */
	public SchedulerContext getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(SchedulerContext context) {
		this.context = context;
	}

	/**
	 * Getter method for action.
	 *
	 * @return the action
	 */
	public SchedulerAction getAction() {
		return action;
	}

	/**
	 * Setter method for action.
	 *
	 * @param action
	 *            the action to set
	 */
	public void setAction(SchedulerAction action) {
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int PRIMER = 31;
		int result = 1;
		result = PRIMER * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SchedulerEntry)) {
			return false;
		}
		SchedulerEntry other = (SchedulerEntry) obj;
		return nullSafeEquals(id, other.id);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchedulerEntry [id=");
		builder.append(id);
		builder.append(", status=");
		builder.append(status);
		builder.append(", actionName=");
		builder.append(actionName);
		builder.append(", configuration=");
		builder.append(configuration);
		builder.append(", context=");
		builder.append(context);
		builder.append(", action=");
		builder.append(action);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "identifier", identifier);
		JsonUtil.addToJson(jsonObject, "action", actionName);
		JsonUtil.addToJson(jsonObject, "status", String.valueOf(getStatus()));
		SchedulerConfiguration schedulerConfiguration = getConfiguration();
		if (schedulerConfiguration instanceof JsonRepresentable) {
			JsonUtil.addToJson(jsonObject, "configuration",
					((JsonRepresentable) schedulerConfiguration).toJSONObject());
		}
		JsonUtil.addToJson(jsonObject, "context", JsonUtil.toJsonObject(context));
		return jsonObject;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// implement me!

	}

	@Override
	public String getContainer() {
		return tenantId;
	}

	@Override
	public void setContainer(String container) {
		tenantId = container;
	}

	/**
	 * Gets the expected execution time. The time is calculated in advance and fixed for the given entry and will not
	 * change until loaded again.
	 *
	 * @return the expected execution time
	 */
	public Date getExpectedExecutionTime() {
		return expectedExecutionTime;
	}

	/**
	 * Sets the expected execution time.
	 *
	 * @param expectedExecutionTime
	 *            the new expected execution time
	 */
	public void setExpectedExecutionTime(Date expectedExecutionTime) {
		this.expectedExecutionTime = expectedExecutionTime;
	}

	/**
	 * Checks if the entry could be removed. This means to be completed and returns <code>true</code> from
	 * {@link SchedulerConfiguration#isRemoveOnSuccess()}.
	 *
	 * @return true, if the entry could be deleted
	 */
	public boolean isForRemoval() {
		return SchedulerEntryStatus.COMPLETED == getStatus() && getConfiguration().isRemoveOnSuccess();
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
}
