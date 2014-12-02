package com.sirma.itt.pm.schedule.service.dao;

import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.BASELINE_END_DATE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.BASELINE_PERCENT_DONE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.BASELINE_START_DATE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.DURATION;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.DURATION_UNIT;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.UNIQUE_IDENTIFIER;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.PLANNED_END_DATE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.TITLE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.DESCRIPTION;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.PLANNED_START_DATE;
import static com.sirma.itt.pm.schedule.model.ScheduleEntryProperties.STATUS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PersistentPropertiesExtension;

/**
 * The list of schedule persistent properties
 * 
 * @author BBonev
 */
@Extension(target = PersistentPropertiesExtension.TARGET_NAME, order = 30)
public class SchedulePersistenProperties implements PersistentPropertiesExtension {

	/** The allowed no definition fields. */
	private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<String>(Arrays.asList(
			BASELINE_END_DATE, BASELINE_PERCENT_DONE, BASELINE_START_DATE, PLANNED_START_DATE, PLANNED_END_DATE,
			DURATION, DURATION_UNIT, DESCRIPTION, TITLE, UNIQUE_IDENTIFIER, STATUS));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getPersistentProperties() {
		return PERSISTENT_PROPERTIES;
	}

}
