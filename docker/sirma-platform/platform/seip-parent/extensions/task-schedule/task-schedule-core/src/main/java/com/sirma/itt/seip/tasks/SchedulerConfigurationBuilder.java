package com.sirma.itt.seip.tasks;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.quartz.CronExpression;

import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Utility class for building {@link SchedulerConfiguration} out of a {@link ControlDefinition}
 * 
 * @author Valeri Tishev
 *
 */
public class SchedulerConfigurationBuilder {

	/**
	 * SchedulerConfigurationBuilder is not meant to be instantiated
	 */
	private SchedulerConfigurationBuilder() {
		// No SchedulerConfigurationBuilder instances for you...
	}


	/**
	 * Builds {@link SchedulerConfiguration} out of given {@link ControlDefinition} 
	 * 
	 * @param identifier identifier of scheduler entry
	 * @param controlDefinition {@link ControlDefinition} containing all control parameters
	 * 
	 * @throws EmfRuntimeException in case a mandatory control parameter is undefined
	 * or the provided {@link CronExpression} is invalid 
	 * 
	 * @return the built {@link SchedulerConfiguration}
	 */
	public static SchedulerConfiguration buildConfiguration(
			String identifier,
			ControlDefinition controlDefinition) {

		Map<String, ControlParam> controlParameters = 
				controlDefinition.getControlParams().stream().collect(
						Collectors.toMap(ControlParam::getName, Function.identity()));

		SchedulerEntryType schedulerEntryType = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.SCHEDULER_TYPE, 
				SchedulerEntryType::valueOf);

		Calendar startDate = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.START_TIME, 
				DatatypeConverter::parseDateTime);

		String cronExpression = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.CRON_EXPRESSION, 
				Function.identity());

		if (!CronExpression.isValidExpression(cronExpression)) {
			throw new EmfRuntimeException("Invalid cron expression [" + cronExpression + "]");
		}

		Boolean isPeristent = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.IS_PERSISTENT, 
				Boolean::valueOf);

		Boolean continueOnError = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.CONTINUE_ON_ERROR, 
				Boolean::valueOf);

		Boolean removeOnSuccess = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.REMOVE_ON_SUCCESS, 
				Boolean::valueOf);

		Integer retryCount = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.RETRY_COUNT, 
				Integer::valueOf);

		Long retryDeylay = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.RETRY_DELAY, 
				Long::valueOf);

		Integer maxRetryCount = getControlParameter(
				controlParameters, 
				TimedTaskConfigurationProperties.MAX_RETRY_COUNT, 
				Integer::valueOf);

		DefaultSchedulerConfiguration schedulerConfiguration = new DefaultSchedulerConfiguration();
		schedulerConfiguration.setIdentifier(identifier);

		return schedulerConfiguration
				.setType(schedulerEntryType)
				.setScheduleTime(startDate.getTime())
				.setCronExpression(cronExpression)
				.setPersistent(isPeristent)
				.setContinueOnError(continueOnError)
				.setRemoveOnSuccess(removeOnSuccess)
				.setRetryCount(retryCount)
				.setRetryDelay(retryDeylay)
				.setMaxRetryCount(maxRetryCount);
	}

	/**
	 * Gets a single control parameter 
	 * 
	 * @param controlParameters {@link Map} containing all provided control parameters
	 * @param configurationProperty the configuration property key
	 * @param function {@link Function} to be invoked on passed value
	 * 
	 * @return the control parameter if provided, or its default value otherwise
	 * 
	 * @throws EmfRuntimeException in case a mandatory configuration is undefined  
	 */
	@SuppressWarnings("unchecked")
	private static <T> T getControlParameter(
			Map<String, ControlParam> controlParameters, 
			TimedTaskConfigurationProperties configurationProperty,
			Function<String, T> function) {

		ControlParam controlParameter = controlParameters.get(configurationProperty.getKey());

		if ((controlParameter == null || controlParameter.getValue() == null) 
				&& configurationProperty.getDefaultValue() == null) {
			throw new EmfRuntimeException("Missing mandatory control parameter with name [" + configurationProperty.key + "]");
		}

		if (controlParameter == null || controlParameter.getValue() == null) {
			return (T) configurationProperty.getDefaultValue();
		}

		return function.apply(controlParameter.getValue());
	}

	/**
	 * Enumeration of timed task configuration property keys and their default values.
	 * 
	 * <B>NOTE</B>: {@code null} default value stands for a mandatory configuration property
	 * 
	 * @author Valeri Tishev
	 *
	 */
	protected enum TimedTaskConfigurationProperties {

		/**
		 * Scheduler type. 
		 * 
		 * @see SchedulerEntryType
		 */
		SCHEDULER_TYPE("type", () -> SchedulerEntryType.CRON),

		/**
		 * Initial execution date.
		 */
		START_TIME("startTime", Calendar::getInstance),

		/**
		 * Cron expression
		 */
		CRON_EXPRESSION("cronExpression", () -> null),

		/**
		 * Is persistent
		 */
		IS_PERSISTENT("persistent", () -> Boolean.FALSE),

		/**
		 * Should continue on error
		 */
		CONTINUE_ON_ERROR("continueOrError", () -> Boolean.TRUE),

		/**
		 * Should be removed on successful execution
		 */
		REMOVE_ON_SUCCESS("removeOnSuccess", () -> Boolean.FALSE),

		/**
		 * Retry count
		 */
		RETRY_COUNT("retryCount", () -> 0),

		/**
		 * Retry delay
		 */
		RETRY_DELAY("retryDelay", () -> 3_600_000L),

		/**
		 * Maximum retries count
		 */
		MAX_RETRY_COUNT("maxRetryCount", () -> 0);


		private String key;

		private Supplier<Serializable> defaultValue;

		TimedTaskConfigurationProperties(String key, Supplier<Serializable> defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		/**
		 * Get the timed task configuration property key
		 * 
		 * @return timed task configuration property key
		 */
		public String getKey() {
			return this.key;
		}

		/**
		 * Get the timed task configuration property default value
		 * 
		 * @return timed task configuration property default value
		 */
		public Serializable getDefaultValue() {
			return this.defaultValue.get();
		}

	}
	
}
