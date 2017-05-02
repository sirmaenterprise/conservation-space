package com.sirma.itt.seip.search.converters;

import javax.json.JsonObject;

import org.joda.time.DateTime;

import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.time.DateRange;

/**
 * Interface for all converters form {@link JsonObject} to {@link DateRange}.
 * 
 * @author Boyan Tonchev
 */
public interface DateRangeConverter extends Plugin {
	
	/**
	 * Check if current implementation can convert <code>operator</code>.
	 * 
	 * @param operator
	 *            - type of operation. For example: within, after, between ...
	 * @return true if can.
	 */
	public boolean canConvert(String operator);

	/**
	 * Convert {@link JsonObject} <code>filterCriteria</code> to {@link DateRange} object.
	 * 
	 * @param dateTime
	 *            - base time object used for calculation.
	 * @param filterCriteria
	 *            - {@link JsonObject} which have to bo converted.
	 * @return converted object.
	 */
	public DateRange convert(DateTime dateTime, JsonObject filterCriteria);
}
