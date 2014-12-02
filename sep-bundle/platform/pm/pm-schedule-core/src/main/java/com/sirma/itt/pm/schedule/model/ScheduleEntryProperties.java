package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * List of schedule entry properties.
 * 
 * @author BBonev
 */
public interface ScheduleEntryProperties extends DefaultProperties {

	/** Default Bryntum Task model properties. */
	/** The schedule start date. */
	String SCHEDULE_START_DATE = "StartDate";
	/** The schedule end date. */
	String SCHEDULE_END_DATE = "EndDate";
	/** The baseline start date. */
	String BASELINE_START_DATE = "BaselineStartDate";
	/** The baseline end date. */
	String BASELINE_END_DATE = "BaselineEndDate";
	/** The baseline percent done. */
	String BASELINE_PERCENT_DONE = "BaselinePercentDone";
	/** The duration. */
	String DURATION = "Duration";
	/** The duration unit. */
	String DURATION_UNIT = "DurationUnit";
	/** The css class. */
	String CSS_CLASS = "cls";
	/** The parent id. */
	String PARENT_ID = "parentId";
	/** The phantom id. */
	String PHANTOM_ID = "PhantomId";
	/** The phantom parent id. */
	String PHANTOM_PARENT_ID = "PhantomParentId";
	/** The expanded. */
	String EXPANDED = "expanded";
	/** The children. */
	String CHILDREN = "children";

	/* PMS specific properties. */
	/** The entry type. */
	String ENTRY_TYPE = "EntryType";
	/** The actual instance id. */
	String ACTUAL_INSTANCE_ID = "ActualInstanceId";
	/** The the start mode to be used for scheduler. */
	String START_MODE = "StartMode";
	/** The index of the task in the scheduler. */
	String INDEX = "index";

	/**
	 * The has dependencies property. Has value of <code>true</code> then there are dependencies
	 * from/to other entry.
	 */
	String HAS_DEPENDENCIES = "hasDependencies";

}
