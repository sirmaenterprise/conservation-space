package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Constants with minifed properties used in the minified scheduler format.
 */
public interface ScheduleEntryMinifiedProperties extends DefaultProperties {

	String TITLE = "title";
	String UNIQUE_IDENTIFIER = "uid";
	String STATUS = "s";
	String TYPE = "tp";

	/** Default Bryntum Task model properties. */
	/** The schedule start date. */
	String SCHEDULE_START_DATE = "sd";
	/** The schedule end date. */
	String SCHEDULE_END_DATE = "ed";
	/** The baseline start date. */
	String BASELINE_START_DATE = "bsd";
	/** The baseline end date. */
	String BASELINE_END_DATE = "bed";
	/** The baseline percent done. */
	String BASELINE_PERCENT_DONE = "bpd";
	/** The duration. */
	String DURATION = "d";
	/** The duration unit. */
	String DURATION_UNIT = "du";
	/** The css class. */
	String CSS_CLASS = "cls";
	/** The parent id. */
	// String PARENT_ID = "pid";
	/** The phantom id. */
	String PHANTOM_ID = "phid";
	/** The phantom parent id. */
	String PHANTOM_PARENT_ID = "phpid";
	/** The expanded. */
	String EXPANDED = "exp";
	/** The children. */
	String CHILDREN = "children";

	String COLOR = "Color";

	/**
	 * Definition type value from concrete codelist. Used and passed only one way from the server to
	 * client and allows title field to be prefixed with definition type for more detailed info.
	 */
	String DEFINITION_TYPE = "defType";

	/* PMS specific properties. */
	/** The entry type. */
	String ENTRY_TYPE = "et";
	/** The actual instance id. */
	String ACTUAL_INSTANCE_ID = "aiid";
	/** The the start mode to be used for scheduler. */
	String START_MODE = "sm";

	/**
	 * The has dependencies property. Has value of <code>true</code> then there are dependencies
	 * from/to other entry.
	 */
	String HAS_DEPENDENCIES = "hasDependencies";
}
