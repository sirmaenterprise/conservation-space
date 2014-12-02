package com.sirma.itt.pm.schedule.util;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;

/**
 * The ScheduleEntryUtil is responsible to provide capsulated specific logic for
 * {@link #ScheduleEntry} classes
 */
public class ScheduleEntryUtil {

	/**
	 * Copy baseline metadata. From the properties map, updates the entry with data for
	 * actual/planned dates
	 *
	 * @param properties
	 *            the properties to use
	 * @param entry
	 *            the entry to update
	 */
	public static void copyBaselineMetadata(Map<String, Serializable> properties,
			ScheduleEntry entry) {
		String propertyToCopy = null;

		// CollectionUtils.copyValue(source, TaskProperties.TASK_OWNER, entry,
		// DefaultProperties.CREATED_BY);
		// if (properties.get(TaskProperties.ACTUAL_START_DATE) == null) {
		propertyToCopy = TaskProperties.PLANNED_START_DATE;
		// } else {
		// propertyToCopy = TaskProperties.ACTUAL_START_DATE;
		entry.getProperties().put(TaskProperties.ACTUAL_START_DATE, properties.get(TaskProperties.ACTUAL_START_DATE));
		// CollectionUtils.copyValue(source, propertyToCopy, entry, propertyToCopy);
		// }
		// set the start date
		entry.getProperties().put(ScheduleEntryProperties.PLANNED_START_DATE,
				properties.get(TaskProperties.PLANNED_START_DATE));
		// CollectionUtils.copyValue(source, TaskProperties.PLANNED_START_DATE, entry,
		// ScheduleEntryProperties.PLANNED_START_DATE);
		entry.getProperties().put(ScheduleEntryProperties.BASELINE_START_DATE,
				properties.get(propertyToCopy));
		// CollectionUtils.copyValue(source, propertyToCopy, entry,
		// ScheduleEntryProperties.BASELINE_START_DATE);
		// if (properties.containsKey(TaskProperties.ACTUAL_END_DATE)) {
		// propertyToCopy = TaskProperties.ACTUAL_END_DATE;
		entry.getProperties().put(TaskProperties.ACTUAL_END_DATE, properties.get(TaskProperties.ACTUAL_END_DATE));
		// } else {
		propertyToCopy = TaskProperties.PLANNED_END_DATE;
		// }
		entry.getProperties().put(ScheduleEntryProperties.BASELINE_END_DATE,
				properties.get(propertyToCopy));
		// CollectionUtils.copyValue(source, propertyToCopy, entry,
		// ScheduleEntryProperties.BASELINE_END_DATE);
		entry.getProperties().put(ScheduleEntryProperties.PLANNED_END_DATE,
				properties.get(TaskProperties.PLANNED_END_DATE));
		// CollectionUtils.copyValue(source, TaskProperties.PLANNED_END_DATE, entry,
		// ScheduleEntryProperties.PLANNED_END_DATE);
	}

	/**
	 * Update schedule entry fields from map of properties.
	 *
	 * @param entry
	 *            the entry to update
	 * @param data
	 *            the data to use as source
	 */
	public static void updateScheduleEntry(ScheduleEntry entry, Map<String, Serializable> data) {

		String propertyToCopy;
		// if (data.get(TaskProperties.ACTUAL_START_DATE) == null) {
		propertyToCopy = TaskProperties.PLANNED_START_DATE;
		// }
		// else {
		// propertyToCopy = TaskProperties.ACTUAL_START_DATE;
		// }
		// set the start date
		entry.setStartDate((Date) data.get(propertyToCopy));

		// if (data.containsKey(TaskProperties.ACTUAL_END_DATE)) {
		// propertyToCopy = TaskProperties.ACTUAL_END_DATE;
		// } else {
		propertyToCopy = TaskProperties.PLANNED_END_DATE;
		// }
		// set the end date
		entry.setEndDate((Date) data.get(propertyToCopy));

	}
}
