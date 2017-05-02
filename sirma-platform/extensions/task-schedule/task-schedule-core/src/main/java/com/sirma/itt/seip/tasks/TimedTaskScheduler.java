package com.sirma.itt.seip.tasks;

import java.util.Iterator;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.definition.jaxb.ObjectType;
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
	private DictionaryService dictionaryService;


	/**
	 * Schedules all defined timers just after definitions are already loaded
	 * 
	 * @param event the caught {@link AllDefinitionsLoaded} event
	 */
	public void scheduleTimedTasks(@Observes AllDefinitionsLoaded event) {
		List<GenericDefinition> allDefinitions = dictionaryService.getAllDefinitions(GenericDefinition.class);

		for (GenericDefinition definition : allDefinitions) {
			if (isTimerDefinition(definition)) {
				Iterator<PropertyDefinition> iterator = definition.fieldsStream().iterator();
				while (iterator.hasNext()) {
					schedule(iterator.next());
				}
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

			SchedulerContext schedulerContext = new SchedulerContext();
			schedulerContext.put(ScriptSchedulerExecutor.PARAM_SCRIPT, 
					propertyDefinition.getDefaultValue());

			LOGGER.debug("Scheduling task with identifier [{}] \n" + 
					"\t SchedulerConfiguration = {} \n" +
					"\t Script to be executed = \n{} \n",
					propertyDefinition.getName(),
					((DefaultSchedulerConfiguration) schedulerConfiguration).toJSONObject().toString(),
					propertyDefinition.getDefaultValue());

			schedulerService.schedule(ScriptSchedulerExecutor.NAME, schedulerConfiguration, schedulerContext);
		} catch (RuntimeException e) {
			LOGGER.error("Failed scheduling task with identifier [{}]", propertyDefinition.getName());
			LOGGER.error("Scheduling failed due to: ", e); 
		}
	}

	/**
	 * Checks whether the passed {@link GenericDefinition} is of type {@link ObjectType#TIMER} 
	 * 
	 * @param definition {@link GenericDefinition} to be checked
	 * @return true if passed {@link GenericDefinition} is of type {@link ObjectType#TIMER}, false otherwise
	 */
	private static boolean isTimerDefinition(GenericDefinition definition) {
		return ObjectType.TIMER.value().equalsIgnoreCase(definition.getType());
	}

}
