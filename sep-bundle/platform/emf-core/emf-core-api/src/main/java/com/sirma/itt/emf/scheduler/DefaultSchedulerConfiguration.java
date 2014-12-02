package com.sirma.itt.emf.scheduler;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Default {@link SchedulerConfiguration} implementation. It's fully configured for working with
 * Kryo tag serializer.<br>
 * The class implements properly the method {@link #getNextScheduleTime()} and uses
 * {@link org.quartz.CronExpression} to parse cron expressions.
 * 
 * @author BBonev
 */
public class DefaultSchedulerConfiguration implements SchedulerConfiguration {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultSchedulerConfiguration.class);

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

	/** The in same tx. */
	@Tag(7)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerEntryType getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setType(SchedulerEntryType type) {
		this.type = type;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRetryCount() {
		return retryCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setRetryCount(int retryCount) {
		this.retryCount = retryCount;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getScheduleTime() {
		return scheduleTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setScheduleTime(Date scheduleTime) {
		this.scheduleTime = scheduleTime;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getNextScheduleTime() {
		Date time = getScheduleTime();
		if (time != null) {
			if (System.currentTimeMillis() < time.getTime()) {
				return time;
			}
			// check if we have configured some retry time to take it into account when calculating
			// the time
			if ((getRetryDelay() != null) && (getRetryDelay() > 0L) && (getRetryCount() > 0)
					&& (getRetryCount() <= getMaxRetryCount())) {
				// if we have incremental delay or not
				int multiplier = incrementalDelay ? getRetryCount() : 1;
				// compute the delay in milliseconds for the given retry count
				long delay = multiplier * getRetryDelay() * 1000;
				LOGGER.trace("Calculating delay for execution " + delay);
				return new Date(time.getTime() + delay);
			}
		}
		if (cronExpression != null) {
			if (parsedExpression == null) {
				try {
					parsedExpression = new CronExpression(cronExpression);
				} catch (ParseException e) {
					LOGGER.warn("Failed to parse cron expression: " + cronExpression, e);
					return null;
				}
			}
			return parsedExpression.getNextValidTimeAfter(new Date());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSynchronous() {
		return synchronous;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setSynchronous(boolean synchronous) {
		this.synchronous = synchronous;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean inSameTransaction() {
		return inSameTx;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setInSameTransaction(boolean inSameTx) {
		this.inSameTx = inSameTx;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventTrigger getEventTrigger() {
		return trigger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setEventTrigger(EventTrigger trigger) {
		this.trigger = trigger;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setPersistent(boolean persistent) {
		this.persistent = persistent;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRetryDelay() {
		return retryDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setRetryDelay(Long delay) {
		this.retryDelay = delay;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIncrementalDelay() {
		return incrementalDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setIncrementalDelay(boolean incremental) {
		this.incrementalDelay = incremental;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRemoveOnSuccess() {
		return removeOnSuccess;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerConfiguration setRemoveOnSuccess(boolean removeOnSuccess) {
		this.removeOnSuccess = removeOnSuccess;
		return this;
	}
}
