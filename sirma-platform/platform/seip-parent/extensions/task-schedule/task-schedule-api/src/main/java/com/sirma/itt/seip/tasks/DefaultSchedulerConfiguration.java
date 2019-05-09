package com.sirma.itt.seip.tasks;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Default {@link SchedulerConfiguration} implementation. It's fully configured for working with Kryo tag serializer.
 * <br>
 * The class implements properly the method {@link #getNextScheduleTime()} and uses {@link org.quartz.CronExpression} to
 * parse cron expressions.
 *
 * @author BBonev
 */
public class DefaultSchedulerConfiguration implements SchedulerConfiguration, JsonRepresentable {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSchedulerConfiguration.class);

	/** The type. */
	@Tag(1)
	protected SchedulerEntryType type;

	/** The retry count. */
	@Tag(2)
	protected int retryCount;

	/** The max retry count. */
	@Tag(3)
	protected int maxRetryCount = 0;

	/** The schedule time. */
	@Tag(4)
	protected Date scheduleTime;

	/** The cron expression. */
	@Tag(5)
	protected String cronExpression;

	/** The synchronous. */
	@Tag(6)
	protected boolean synchronous = true;

	/**
	 * The in same tx. This fields is no longer used. Use {@link #transactionMode} instead. This field SHOULD NOT BE
	 * REMOVED or will break all old configurations.
	 *
	 * @see #transactionMode
	 * @deprecated Use {@link #transactionMode} instead.
	 */
	@Tag(7)
	@Deprecated
	protected boolean inSameTx = true;

	/** The trigger. */
	@Tag(8)
	protected EventTrigger trigger;

	/** The parsed expression. */
	private transient CronExpression parsedExpression;

	/** The persistent. */
	@Tag(9)
	protected boolean persistent = true;

	/** The identifier. */
	@Tag(10)
	protected String identifier;
	/** if retry is triggered, wait for the specified time. */
	@Tag(11)
	protected Long retryDelay;

	/** The to specify if the delay is incremental or not. */
	@Tag(12)
	protected boolean incrementalDelay;

	/** The remove on success. */
	@Tag(14)
	protected boolean removeOnSuccess = false;

	/** The transaction mode. Replaces the field {@link #inSameTx}. */
	@Tag(15)
	protected TransactionMode transactionMode = TransactionMode.REQUIRED;

	@Tag(16)
	protected boolean continueOrError = false;

	/**
	 * The in same runAs. This fields is no longer used. Use {@link #runAs} instead. This field SHOULD NOT BE REMOVED or
	 * will break all old configurations.
	 *
	 * @see #runAs
	 * @deprecated Use {@link #runAs} instead.
	 */
	@Tag(17)
	@Deprecated
	protected boolean systemSpecific = true;

	@Tag(18)
	protected RunAs runAs = RunAs.DEFAULT;

	@Tag(19)
	protected String userSystemId;

	@Tag(20)
	protected String group;

	@Tag(21)
	protected int maxActive;

	@Tag(22)
	private String timeZoneID;

	public DefaultSchedulerConfiguration() {
	}

	public DefaultSchedulerConfiguration(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public SchedulerEntryType getType() {
		return type;
	}

	@Override
	public SchedulerConfiguration setType(SchedulerEntryType type) {
		this.type = type;
		return this;
	}

	@Override
	public int getRetryCount() {
		return retryCount;
	}

	@Override
	public SchedulerConfiguration setRetryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}

	@Override
	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	@Override
	public SchedulerConfiguration setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
		return this;
	}

	@Override
	public Date getScheduleTime() {
		if (scheduleTime == null) {
			return null;
		}
		return new Date(scheduleTime.getTime());
	}

	@Override
	public SchedulerConfiguration setScheduleTime(Date scheduleTime) {
		if (scheduleTime == null) {
			this.scheduleTime = null;
		} else {
			this.scheduleTime = new Date(scheduleTime.getTime());
		}
		return this;
	}

	@Override
	@SuppressWarnings("boxing")
	public Date getNextScheduleTime() {
		Date time = getScheduleTime();
		if (time != null) {
			if (System.currentTimeMillis() < time.getTime()) {
				return time;
			}
			// check if we have configured some retry time to take it into account when calculating
			// the time
			long delay = 0L;
			if (getRetryDelay() != null) {
				delay = getRetryDelay();
			}
			if (delay > 0L && getRetryCount() > 0 && getRetryCount() <= getMaxRetryCount()) {
				// if we have incremental delay or not
				int multiplier = incrementalDelay ? getRetryCount() : 1;
				// compute the delay in milliseconds for the given retry count
				delay = multiplier * delay;
				LOGGER.trace("Calculating delay for execution {} ms", delay * 1000);
				Calendar currTime = Calendar.getInstance();
				currTime.add(Calendar.SECOND, (int) delay);
				return currTime.getTime();
			}
		}
		if (cronExpression != null) {
			return getNextScheduleTimeFromCronExpression();
		}
		return null;
	}

	/**
	 * Gets the next schedule time from cron expression.
	 *
	 * @return the next schedule time from cron expression
	 */
	private Date getNextScheduleTimeFromCronExpression() {
		if (parsedExpression == null) {
			try {
				parsedExpression = new CronExpression(cronExpression);
				if (timeZoneID != null) {
					parsedExpression.setTimeZone(TimeZone.getTimeZone(timeZoneID));
				}
			} catch (ParseException e) {
				LOGGER.warn("Failed to parse cron expression: " + cronExpression, e);
				return null;
			}
		}
		return parsedExpression.getNextValidTimeAfter(new Date());
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public SchedulerConfiguration setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
		return this;
	}

	@Override
	public boolean isSynchronous() {
		return synchronous;
	}

	@Override
	public SchedulerConfiguration setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
		return this;
	}

	@Override
	public EventTrigger getEventTrigger() {
		return trigger;
	}

	@Override
	public SchedulerConfiguration setEventTrigger(EventTrigger trigger) {
		this.trigger = trigger;
		return this;
	}

	@Override
	public boolean isPersistent() {
		return persistent;
	}

	@Override
	public SchedulerConfiguration setPersistent(boolean persistent) {
		this.persistent = persistent;
		return this;
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
	public Long getRetryDelay() {
		return retryDelay;
	}

	@Override
	public SchedulerConfiguration setRetryDelay(Long delay) {
		retryDelay = delay;
		return this;
	}

	@Override
	public boolean isIncrementalDelay() {
		return incrementalDelay;
	}

	@Override
	public SchedulerConfiguration setIncrementalDelay(boolean incremental) {
		incrementalDelay = incremental;
		return this;
	}

	@Override
	public boolean isRemoveOnSuccess() {
		return removeOnSuccess;
	}

	@Override
	public SchedulerConfiguration setRemoveOnSuccess(boolean removeOnSuccess) {
		this.removeOnSuccess = removeOnSuccess;
		return this;
	}

	@Override
	public TransactionMode getTransactionMode() {
		return transactionMode;
	}

	@Override
	public SchedulerConfiguration setTransactionMode(TransactionMode transactionMode) {
		this.transactionMode = transactionMode;
		return this;
	}

	@Override
	@SuppressWarnings("boxing")
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "type", String.valueOf(getType()));
		JsonUtil.addToJson(object, "retryCount", getRetryCount());
		JsonUtil.addToJson(object, "maxRetryCount", getMaxRetryCount());
		JsonUtil.addToJson(object, "scheduleTime",
				TypeConverterUtil.getConverter().convert(String.class, getScheduleTime()));
		JsonUtil.addToJson(object, "cronExpression", getCronExpression());
		JsonUtil.addToJson(object, "synchronous", isSynchronous());
		JsonUtil.addToJson(object, "persistent", isPersistent());
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		JsonUtil.addToJson(object, "retryDelay", getRetryDelay());
		JsonUtil.addToJson(object, "incrementalDelay", isIncrementalDelay());
		JsonUtil.addToJson(object, "removeOnSuccess", isRemoveOnSuccess());
		JsonUtil.addToJson(object, "transactionMode", String.valueOf(getTransactionMode()));
		JsonUtil.addToJson(object, "continueOrError", shouldContinueOnError());
		JsonUtil.addToJson(object, "nextScheduleTime",
				TypeConverterUtil.getConverter().convert(String.class, getNextScheduleTime()));
		JsonUtil.addToJson(object, "executionContext", getRunAs());
		JsonUtil.addToJson(object, "group", getGroup());
		JsonUtil.addToJson(object, "maxActive", getMaxActivePerGroup());
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// implement me!
	}

	@Override
	public SchedulerConfiguration setContinueOnError(boolean continueOrError) {
		this.continueOrError = continueOrError;
		return this;
	}

	@Override
	public boolean shouldContinueOnError() {
		return continueOrError;
	}

	@Override
	public RunAs getRunAs() {
		return runAs;
	}

	@Override
	public SchedulerConfiguration setRunAs(RunAs executionContext) {
		runAs = executionContext;
		return this;
	}

	@Override
	public SchedulerConfiguration setRunAs(String userSystemId) {
		this.userSystemId = userSystemId;
		return this;
	}

	@Override
	public String getRunUserId() {
		return userSystemId;
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public int getMaxActivePerGroup() {
		return maxActive;
	}

	@Override
	public SchedulerConfiguration setMaxActivePerGroup(String group, int maxActive) {
		this.group = group;
		this.maxActive = maxActive;
		return this;
	}

	@Override
	public String getTimeZoneID() {
		return timeZoneID;
	}

	@Override
	public SchedulerConfiguration setTimeZoneID(String timeZoneID) {
		this.timeZoneID = timeZoneID;
		// reset parser time zone
		parsedExpression = null;
		return this;
	}

}
