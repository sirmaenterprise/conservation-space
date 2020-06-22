package com.sirma.itt.seip.search.converters;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.time.DateRange;

/**
 * Abstract class for all {@link DateRange} converters it have usefully methods.
 * 
 * @author Boyan Tonchev
 *
 */
public abstract class AbstractDateRangeConverter implements DateRangeConverter {
	
	/**
	 * Pugin name for all date range converters.
	 */
	public static final String PLUGIN_NAME = "date-range-parser";

	@Inject
	protected DateConverter dateConverter;
	
	@Override
	public boolean canConvert(String operator) {
		return getSupportedOperators().contains(operator);
	}
	
	/**
	 * Convert <code>date</code> to {@link Date} object.
	 * @param date - the date as string.
	 * @return converted <code>date</code> or null.
	 */
	protected Date convertToDate(String date) {
		return StringUtils.isNotBlank(date) ? dateConverter.parseDate(date) : null;
	}
	
	/**
	 * @return name of supported operation. For example:  within, after, between ...
	 */
	protected abstract List<String> getSupportedOperators();
}
