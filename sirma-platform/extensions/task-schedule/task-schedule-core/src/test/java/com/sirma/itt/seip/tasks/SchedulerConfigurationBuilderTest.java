package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.SchedulerConfigurationBuilder.TimedTaskConfigurationProperties;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.ControlParamMock;

/**
 * Unit tests for {@link SchedulerConfigurationBuilder}
 * 
 * @author Valeri Tishev
 *
 */
public class SchedulerConfigurationBuilderTest {

	private static final String EXPECTED_IDENTIFIER = "sampleIndetifier";

	/**
	 *  Test building faulty {@link SchedulerConfiguration} with invalid CRON expression
	 */
	@Test(expected = EmfRuntimeException.class)
	public void builtFaultyCronExpressionConfiguration() {
		ControlDefinition createControlDefinition = createControlDefinition(
				createControlParam(TimedTaskConfigurationProperties.CRON_EXPRESSION.getKey(), "invalid cron expression")
				);
		SchedulerConfigurationBuilder.buildConfiguration(EXPECTED_IDENTIFIER, createControlDefinition);
	}

	/**
	 * Test building faulty {@link SchedulerConfiguration} with no configuration properties provided
	 */
	@Test(expected = EmfRuntimeException.class)
	public void builtFaultyConfigurationWithMissingMandatoryField() {
		ControlDefinition createControlDefinition = createControlDefinition();
		SchedulerConfigurationBuilder.buildConfiguration(EXPECTED_IDENTIFIER, createControlDefinition);
	}

	/**
	 * Test building faulty {@link SchedulerConfiguration} with missing cron expression value
	 */
	@Test(expected = EmfRuntimeException.class)
	public void builtFaultyConfigurationWithMissingMandatoryFieldValue() {
		ControlDefinition createControlDefinition = createControlDefinition(
				createControlParam(TimedTaskConfigurationProperties.CRON_EXPRESSION.getKey(), null)
				);
		SchedulerConfigurationBuilder.buildConfiguration(EXPECTED_IDENTIFIER, createControlDefinition);
	}

	/**
	 * Test building a proper {@link SchedulerConfiguration} with minimum configuration properties
	 */
	@Test
	public void buildMinimalConfiguration() {
		final String expectedCronExpression = "0 0/5 * 1/1 * ? *";

		ControlDefinition createControlDefinition = createControlDefinition(
				createControlParam(TimedTaskConfigurationProperties.CRON_EXPRESSION.getKey(), expectedCronExpression)
				);

		SchedulerConfiguration builtConfiguration = 
				SchedulerConfigurationBuilder.buildConfiguration(EXPECTED_IDENTIFIER, createControlDefinition);

		assertEquals(EXPECTED_IDENTIFIER, builtConfiguration.getIdentifier());
		assertEquals(TimedTaskConfigurationProperties.SCHEDULER_TYPE.getDefaultValue(), builtConfiguration.getType());
		assertNotNull(builtConfiguration.getScheduleTime().getTime());
		assertEquals(expectedCronExpression, builtConfiguration.getCronExpression());
		assertEquals(TimedTaskConfigurationProperties.IS_PERSISTENT.getDefaultValue(), builtConfiguration.isPersistent());
		assertEquals(TimedTaskConfigurationProperties.CONTINUE_ON_ERROR.getDefaultValue(), builtConfiguration.shouldContinueOnError());
		assertEquals(TimedTaskConfigurationProperties.REMOVE_ON_SUCCESS.getDefaultValue(), builtConfiguration.isRemoveOnSuccess());
		assertEquals(TimedTaskConfigurationProperties.RETRY_COUNT.getDefaultValue(), builtConfiguration.getRetryCount());
		assertEquals(TimedTaskConfigurationProperties.RETRY_DELAY.getDefaultValue(), builtConfiguration.getRetryDelay());
		assertEquals(TimedTaskConfigurationProperties.MAX_RETRY_COUNT.getDefaultValue(), builtConfiguration.getMaxRetryCount());
	}

	/**
	 * Test building a proper {@link SchedulerConfiguration} with all configuration properties available
	 */
	@Test
	public void buildFullConfiguration() {

		final SchedulerEntryType expectedType = SchedulerEntryType.TIMED;

		final String expectedStartDateConfiguration = "2016-02-08T14:20:00.000+02:00";
		final Date expectedStartTime = DatatypeConverter.parseDateTime(expectedStartDateConfiguration).getTime();

		final String expectedCronExpression = "0 0/5 * 1/1 * ? *";
		final Boolean expectedPersistent = Boolean.TRUE;
		final Boolean expectedContinueOrError = Boolean.FALSE;
		final Boolean expectedRemoveOnSuccess = Boolean.FALSE;
		final Integer expectedRetryCount = 100;
		final Long expectedRetryDelay = 100L;
		final Integer expectedMaxRetryCount = 42;

		ControlDefinition createControlDefinition = createControlDefinition(
				createControlParam(TimedTaskConfigurationProperties.SCHEDULER_TYPE.getKey(), expectedType.toString()),
				createControlParam(TimedTaskConfigurationProperties.START_TIME.getKey(), expectedStartDateConfiguration),
				createControlParam(TimedTaskConfigurationProperties.CRON_EXPRESSION.getKey(), expectedCronExpression),
				createControlParam(TimedTaskConfigurationProperties.IS_PERSISTENT.getKey(), expectedPersistent.toString()),
				createControlParam(TimedTaskConfigurationProperties.CONTINUE_ON_ERROR.getKey(), expectedContinueOrError.toString()),
				createControlParam(TimedTaskConfigurationProperties.REMOVE_ON_SUCCESS.getKey(), expectedRemoveOnSuccess.toString()),
				createControlParam(TimedTaskConfigurationProperties.RETRY_COUNT.getKey(), expectedRetryCount.toString()),
				createControlParam(TimedTaskConfigurationProperties.RETRY_DELAY.getKey(), expectedRetryDelay.toString()),
				createControlParam(TimedTaskConfigurationProperties.MAX_RETRY_COUNT.getKey(), expectedMaxRetryCount.toString())
				);

		SchedulerConfiguration builtConfiguration = 
				SchedulerConfigurationBuilder.buildConfiguration(EXPECTED_IDENTIFIER, createControlDefinition);

		assertEquals(EXPECTED_IDENTIFIER, builtConfiguration.getIdentifier());
		assertEquals(expectedType, builtConfiguration.getType());
		assertEquals(expectedStartTime, builtConfiguration.getScheduleTime());
		assertEquals(expectedCronExpression, builtConfiguration.getCronExpression());
		assertEquals(expectedPersistent, builtConfiguration.isPersistent());
		assertEquals(expectedContinueOrError, builtConfiguration.shouldContinueOnError());
		assertEquals(expectedRemoveOnSuccess, builtConfiguration.isRemoveOnSuccess());
		assertEquals(expectedRetryCount.intValue(), builtConfiguration.getRetryCount());
		assertEquals(expectedRetryDelay, builtConfiguration.getRetryDelay());
		assertEquals(expectedMaxRetryCount.intValue(), builtConfiguration.getMaxRetryCount());
	}

	/**
	 * Creates a {@link ControlDefinition} object
	 * 
	 * @param controlParameters control parameters
	 * @return the built {@link ControlDefinition}
	 */
	private ControlDefinition createControlDefinition(ControlParam... controlParameters) {
		ControlDefintionMock controlDefinition = new ControlDefintionMock();	
		controlDefinition.setControlParams(Arrays.asList(controlParameters));

		return controlDefinition;
	}

	/**
	 * Creates a {@link ControlParam} object
	 * 
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return the build {@link ControlParam}
	 */
	private ControlParam createControlParam(String name, String value) {
		ControlParamMock controlParameter = new ControlParamMock();
		controlParameter.setName(name);
		controlParameter.setValue(value);

		return controlParameter;
	}

}
