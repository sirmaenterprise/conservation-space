package com.sirma.itt.pm.schedule.constants;

import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;

/**
 * Runtime configuration properties that are specific for the schedule module
 * 
 * @author BBonev
 */
public interface ScheduleRuntimeConfiguration extends RuntimeConfigurationProperties {

	/**
	 * Runtime configuration property that holds the current schedule entry when created via
	 * schedule.
	 */
	String CURRENT_SCHEDULE_ENTRY = "$CURRENT_SCHEDULE_ENTRY$";

}
