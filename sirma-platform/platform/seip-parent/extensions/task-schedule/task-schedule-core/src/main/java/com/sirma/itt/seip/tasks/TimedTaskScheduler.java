package com.sirma.itt.seip.tasks;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.script.ScriptSchedulerExecutor;

/**
 * Timed task scheduler.
 *
 * <P>Collects and schedules all defined timers just after all definitions are loaded.</P>
 *
 * <P>A sample timer configuration should be defined as follows:</P>
 *
 * <PRE>
 * {@code
 * <definition id="timedActions" type="timer">
 * 	<fields>
 * 		<field name="cronJob" type="an..200" displayType="system">
 * 			<value>
 * 				<![CDATA[
 * 	    			// execute defined instructions below via the Server side Javascript API
 * 	    			// each day at 00:00 starting from 20.02.2016 at 00:00:00 onwards
 * 				]]>
 * 			</value>
 * 			<control id="schedulerConfiguration">
 * 				<control-param id="config" name="startTime">2016-02-20T00:00:00.000+02:00</control-param>
 * 				<control-param id="config" name="persistent">false</control-param>
 * 				<control-param id="config" name="cronExpression">0 0 0 1/1 * ? *</control-param>
 * 				<control-param id="config" name="removeOnSuccess">false</control-param>
 * 			</control>
 * 		</field>
 * 	</fields>
 * </definition>
 * }
 * </PRE>
 *
 * @author Valeri Tishev
 *
 */
@Singleton
public class TimedTaskScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimedTaskScheduler.class);

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SystemConfiguration configurations;

	/**
	 * Schedules all defined timers just after definitions are already loaded
	 *
	 * @param event the caught {@link DefinitionsChangedEvent} event
	 */
	public void scheduleTimedTasks(@Observes DefinitionsChangedEvent event) {
		List<GenericDefinition> allDefinitions = definitionService.getAllDefinitions(GenericDefinition.class);

		for (GenericDefinition definition : allDefinitions) {
			if (isTimerDefinition(definition)) {
				definition.fieldsStream().forEach(this::schedule);
			}
		}
	}

	/**
	 * Schedules a single timer as defined in its {@link PropertyDefinition}
	 *
	 * @param propertyDefinition the property definition
	 */
	private void schedule(PropertyDefinition propertyDefinition) {
		try {
			SchedulerConfiguration schedulerConfiguration =
					SchedulerConfigurationBuilder.buildConfiguration(propertyDefinition.getName(),
							propertyDefinition.getControlDefinition());
			schedulerConfiguration.setTimeZoneID(configurations.getTimeZoneID().get().toString());

			SchedulerContext schedulerContext = new SchedulerContext();
			schedulerContext.put(ScriptSchedulerExecutor.PARAM_SCRIPT, propertyDefinition.getDefaultValue());

			if (LOGGER.isDebugEnabled()) {
				String configurationJson = ((DefaultSchedulerConfiguration) schedulerConfiguration).toJSONObject()
						.toString();
				LOGGER.debug("Scheduling task with identifier [{}] \n\t SchedulerConfiguration = {} \n" +
								"\t Script to be executed = \n{} \n", propertyDefinition.getName(), configurationJson,
						propertyDefinition.getDefaultValue());
			}

			schedulerService.schedule(ScriptSchedulerExecutor.NAME, schedulerConfiguration, schedulerContext);
		} catch (RuntimeException e) {
			LOGGER.error("Failed scheduling task with identifier [{}] due to: ", propertyDefinition.getName(), e);
		}
	}

	/**
	 * Checks whether the passed {@link GenericDefinition} is of type TIMER
	 *
	 * @param definition {@link GenericDefinition} to be checked
	 * @return true if passed {@link GenericDefinition} is of type TIMER, false otherwise
	 */
	private static boolean isTimerDefinition(GenericDefinition definition) {
		return "TIMER".equalsIgnoreCase(definition.getType());
	}
}
