package com.sirma.itt.pm.schedule.constants;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Defines all configuration name properties for schedule module.
 * 
 * @author BBonev
 */
@Documentation("Schedule specific configurations")
public interface ScheduleConfigProperties extends Configuration {
	/** The auto start mode for schedule entry. Default value: Auto */
	@Documentation("The auto start mode for schedule entry. Default value: Auto")
	String SCHEDULE_START_MODE_AUTO = "schedule.startMode.auto";

	/** The manual start mode for schedule entry. Default value: Manual */
	@Documentation("The manual start mode for schedule entry. Default value: Manual")
	String SCHEDULE_START_MODE_MANUAL = "schedule.startMode.manual";

	/** Enables or disables automatic starting of tasks in the past. Default value: false */
	@Documentation("Enables or disables automatic starting of tasks in the past. Default value: false")
	String SCHEDULE_ALLOW_AUTOSTARTING_OF_TASKS_IN_THE_PAST = "schedule.config.allowAutoStartingTaskInThePast";

	/** Enables or disables actual instance actions evaluations. Default value: false */
	@Documentation("Enables or disables actual instance actions evaluations. Default value: false")
	String SCHEDULE_ACTIONS_EVAL_ACTUAL_INSTANCE = "schedule.actions.evaluateActualInstance";

	/**
	 * Enables or disables actual instance role evaluation This means if actual instance is present
	 * when evaluating user role/actions the particular user role will be used for the specific
	 * entry rather the global project role. Default value: false
	 */
	@Documentation("Enables or disables actual instance role evaluation This means if actual instance is present when evaluating user role/actions the particular user role will be used for the specific entry rather the global project role. Default value: false")
	String SCHEDULE_ACTIONS_EVAL_ACTUAL_INSTANCE_ROLE = "schedule.actions.evaluateActualInstanceRole";

	@Documentation("Codelist number of project definition type. <b>Default value is: 2</b>")
	String CODELIST_PROJECT_DEFINITION = "codelist.projectDefinition";

}
